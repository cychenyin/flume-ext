package com.ganji.cateye.flume.scribe;

import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.FlumeException;
import org.apache.flume.api.HostInfo;
import org.apache.flume.api.RpcClientConfigurationConstants;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ganji.cateye.flume.AbstractMultiThreadRpcClient;
//import com.ganji.cateye.flume.scribe.thrift.LogEntry;
//import com.ganji.cateye.flume.scribe.thrift.ResultCode;
//import com.ganji.cateye.flume.scribe.thrift.scribe;
import org.apache.flume.source.scribe.*;

/**
 * ScribeRpcClient 保证事务状态
 * 
 * @author asdf
 * 
 */
public class ScribeRpcClient extends AbstractMultiThreadRpcClient {
	private static final Logger logger = LoggerFactory.getLogger(ScribeRpcClient.class);
	private String hostname;
	private int port;
	private String name = "";
	private String categoryHeaderKey = null;
	private String serializerName = "scribe";
	private MessageSerializer serializer = null;

	// private scribe.Client client;
	private org.apache.flume.source.scribe.Scribe.Client client;
	private TTransport transport;

	public ScribeRpcClient() {
	}

	@Override
	public void configure(Properties properties) throws FlumeException {
		if (isActive()) {
			throw new FlumeException("Attempting to re-configured an already configured client!");
		}
		stateLock.lock();
		try {
			List<HostInfo> hosts = HostInfo.getHostInfoList(properties);
			if (hosts.size() > 0) {
				HostInfo host = hosts.get(0);
				hostname = host.getHostName();
				port = host.getPortNumber();
			} else {
				hostname = properties.getProperty(ScribeSinkConsts.CONFIG_HOSTNAME, ScribeSinkConsts.DEFAULT_HOSTNAME);
				port = Integer.parseInt(properties.getProperty(ScribeSinkConsts.CONFIG_PORT, ScribeSinkConsts.DEFAULT_PORT));
			}
			name = String.format("%d@%s:%d", new Random().nextInt(), hostname, port);

			// serialization
			serializerName = properties.getProperty(ScribeSinkConsts.CONFIG_SERIALIZER, ScribeSinkConsts.DEFAULT_SERIALIZER);
			if (serializerName.equalsIgnoreCase(ScribeSinkConsts.DEFAULT_SERIALIZER)) {
				serializer = new ScribeSerializer();
			}
			else if (serializerName.equalsIgnoreCase("plain-message")) {
				serializer = new PlainMessageSerializer();
			}
			else {
				try {
					serializer = (MessageSerializer) Class.forName(serializerName).newInstance();
				} catch (Exception ex) {
					throw new RuntimeException("invalid serializer specified", ex);
				}
			}
			categoryHeaderKey = properties.getProperty(ScribeSinkConsts.CONFIG_CATEGORY_HEADER_KEY
					, ScribeSinkConsts.DEFAULT_CATEGORY_HEADER_KEY);

			Context context = new Context();
			context.put(ScribeSinkConsts.CONFIG_SERIALIZER, serializerName);
			context.put(ScribeSinkConsts.DEFAULT_CATEGORY_HEADER_KEY, categoryHeaderKey);
			serializer.configure(context);

			// sink global
			batchSize = Integer.parseInt(properties.getProperty(ScribeSinkConsts.CONFIG_BATCHSIZE, ScribeSinkConsts.DEFAULT_BATCHSIZE));
			requestTimeout = Long.parseLong(properties.getProperty(
					RpcClientConfigurationConstants.CONFIG_REQUEST_TIMEOUT,
					String.valueOf(RpcClientConfigurationConstants.DEFAULT_REQUEST_TIMEOUT_MILLIS)));
			// 请求时长最短1s
			if (requestTimeout < ScribeSinkConsts.MIN_REQUEST_TIMEOUT_MILLIS) {
				logger.warn("Request timeout specified less than 1s. Using default value instead.");
				requestTimeout = RpcClientConfigurationConstants.DEFAULT_REQUEST_TIMEOUT_MILLIS;
			}

			try {
				// logger.debug("scribeSink.host={} port={}", hostname, port);
				transport = new TFramedTransport(new TSocket(new Socket(hostname, port)));
				client = new Scribe.Client(new TBinaryProtocol(transport, false, false));
				// logger.debug("scribeSink has created transport succesfully");
			} catch (SocketException ex) {
				logger.warn("Unable to create Thrift Transport cause of socket exception. sleep 1s then. host=" + hostname + ":port=" + port, ex);
				Thread.sleep(ScribeSinkConsts.MIN_REQUEST_TIMEOUT_MILLIS);
				throw new RuntimeException(ex);
			} catch (Exception ex) {
				logger.warn("Unable to create Thrift Transport, host=" + hostname + ":port=" + port, ex);
				throw new RuntimeException(ex);
			}

			connState = State.READY;
		} catch (Throwable ex) {
			// Failed to configure, kill the client.
			connState = State.DEAD;
			if (ex instanceof Error) {
				throw (Error) ex;
			} else if (ex instanceof RuntimeException) {
				throw (RuntimeException) ex;
			}
			throw new FlumeException("Error while configuring RpcClient. ", ex);
		} finally {
			stateLock.unlock();
		}
	}

	@Override
	public void close() throws FlumeException {
		try {
			// Do not release this, because this client is not to be used again
			stateLock.lock();
			connState = State.DEAD;

			transport.close();
			client = null;

			logger.info("SribeRpcClient closed. name={}", name);
		} catch (Throwable ex) {
			if (ex instanceof Error) {
				throw (Error) ex;
			} else if (ex instanceof RuntimeException) {
				throw (RuntimeException) ex;
			}
			throw new FlumeException("Failed to close SribeRpcClient. ", ex);
		} finally {
			stateLock.unlock();
		}
	}

	@SuppressWarnings("unused")
	private void dump(Properties properties) {
		for (Object key : properties.keySet()) {
			logger.warn("ScribeRpcClient dump conifg {}={}", key.toString(), properties.getProperty(key.toString()));
		}
	}

	@Override
	public void doAppendBatch(List<Event> events) throws Exception {
		// group log by route config
		List<LogEntry> items = new ArrayList<LogEntry>();
		for (Event event : events) {
			items.add(serializer.serialize(event));
		}
		ResultCode resultCode = client.Log(items);
		if (!resultCode.equals(ResultCode.OK)) {
			// 为了防止服务器状态恢复后的突发压力，sleep一个随机的时间; 最大2s= 2000ms
			Thread.sleep((new Random()).nextInt(2000));
			throw new Exception("scribe client return try later");
		} 
	}

	public boolean equals(Object o) {
		if (o == null || !(o instanceof ScribeRpcClient)) {
			return false;
		}
		ScribeRpcClient r = (ScribeRpcClient) o;
		if (r.client != null && r.client.equals(this.client)) {
			return true;
		}
		return false;
	}

	public int hashCode() {
		return client == null ? 0 : client.hashCode();
	}

	@Override
	public String getName() {
		return name;
	}

}
