package com.ganji.cateye.flume.scribe;

import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang.StringUtils;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.EventDeliveryException;
import org.apache.flume.FlumeException;
import org.apache.flume.api.HostInfo;
import org.apache.flume.api.RpcClientConfigurationConstants;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ganji.cateye.flume.AbstractMultiThreadRpcClient;
import com.ganji.cateye.flume.MessageSerializer;
import com.ganji.cateye.flume.PlainMessageSerializer;
import com.ganji.cateye.flume.ScribeSerializer;
import com.ganji.cateye.flume.scribe.thrift.LogEntry;
import com.ganji.cateye.flume.scribe.thrift.ResultCode;
import com.ganji.cateye.flume.scribe.thrift.scribe;
import com.ganji.cateye.utils.StatsDClientHelper;

/**
 * ScribeRpcClient 保证事务状态
 * 
 * @author asdf
 * 
 */
public class ScribeRpcClient extends AbstractMultiThreadRpcClient {
	private static final Logger logger = LoggerFactory.getLogger(ScribeRpcClient.class);
	private final Lock stateLock;
	private State connState;
	private String hostname;
	private int port;
	private String name = "";
	private String categoryHeaderKey = null;
	private String serializerName = "scribe";
	private MessageSerializer serializer = null;

	private scribe.Client client;
	private TTransport transport;

	public ScribeRpcClient() {
		stateLock = new ReentrantLock(true);
		connState = State.INIT;
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
			if (requestTimeout < 1000) {
				logger.warn("Request timeout specified less than 1s. Using default value instead.");
				requestTimeout = RpcClientConfigurationConstants.DEFAULT_REQUEST_TIMEOUT_MILLIS;
			}

			try {
				logger.warn("scribeSink.host={} port={}", hostname, port);
				transport = new TFramedTransport(new TSocket(new Socket(hostname, port)));
				client = new scribe.Client(new TBinaryProtocol(transport, false, false));
				logger.warn("scribeSink has created transport succesfully");
			} catch (SocketException ex) {
				logger.error("Unable to create Thrift Transport cause of socket exception. sleep 1s then. host=" + hostname + ":port=" + port, ex);
				Thread.sleep(1000);
				throw new RuntimeException(ex);
			} catch (Exception ex) {
				logger.error("Unable to create Thrift Transport, host=" + hostname + ":port=" + port, ex);
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
	public void append(Event event) throws EventDeliveryException {
		List<Event> events = new ArrayList<Event>();
		events.add(event);
		this.appendBatch(events);
		throw new EventDeliveryException("not support, use appendBatch please;");
	}
// 用来做测试事务回滚的代码
//	private boolean stub_fired = false;
//	// return true when first "test 2" occur
//	private boolean stub_check(List<Event> events) {
//		if(stub_fired == true) 
//			return false;
//		for(Event event : events) {
//			String body = null;
//			try {
//				body = new String(event.getBody(), "UTF-8").replace("\n", "");
//			} catch (UnsupportedEncodingException e) {
//			}
//			if(body.contains("test 2")) {
//				stub_fired = true;
//			}
//		}
//		return stub_fired;
//	}
	
	@Override
	public void appendBatch(final List<Event> events) throws EventDeliveryException {
		try {
			if (!isActive()) {
				throw new EventDeliveryException("Client was closed due to error.  Please create a new client");
			}
			// group log by route config
			List<LogEntry> items = new ArrayList<LogEntry>();
			for (Event event : events) {
				items.add(serializer.serialize(event));
			}
//			if(stub_check(events)) {
//				throw new EventDeliveryException("test trans rollback");
//			}
			
			ResultCode resultCode = client.Log(items);
			if (!resultCode.equals(ResultCode.OK)) {
//				for (Event event : events) {
//					//items.add(serializer.serialize(event));
//					logger.error("fail. " + new String(event.getBody(), "UTF-8").replace("\n", "") );
//				}
				// 为了防止服务器状态恢复后的突发压力，sleep一个随机的时间; 最大2s= 2000ms
				Thread.sleep((new Random()).nextInt(2000));
				throw new Exception("scribe client return try later");
			} 
			else {
////				for (Event event : events) {
////					//items.add(serializer.serialize(event));
////					logger.warn("done. " + new String(event.getBody(), "UTF-8").replace("\n", "") );
////				}
				logger.info("scribe rpc send successfully. size=" + items.size());
			}
		} catch (Throwable e) {
			if (e instanceof ExecutionException) {
				Throwable cause = e.getCause();
				if (cause instanceof TException || cause instanceof InterruptedException) {
					throw new EventDeliveryException("Send call failure cause of rocketmq exception. ", cause);
				} else if (cause instanceof TimeoutException) {
					throw new EventDeliveryException("Send call timeout", cause);
				}
			}
			if (e instanceof Error) {
				throw (Error) e;
			} else if (e instanceof RuntimeException) {
				throw (RuntimeException) e;
			}
			throw new EventDeliveryException("Failed to send event. ", e);
		}
	}

	@Override
	public boolean isActive() {
		stateLock.lock();
		try {
			return (connState == State.READY);
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

	// rocketmq producor api不支持批量， 所有这里是一个awful实现

	private static enum State {
		INIT, READY, DEAD
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
