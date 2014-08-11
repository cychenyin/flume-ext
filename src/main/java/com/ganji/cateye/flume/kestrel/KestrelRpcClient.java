package com.ganji.cateye.flume.kestrel;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.Random;

import org.apache.commons.lang.StringUtils;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.EventDeliveryException;
import org.apache.flume.FlumeException;
import org.apache.flume.api.AbstractRpcClient;
import org.apache.flume.api.HostInfo;
import org.apache.flume.api.RpcClientConfigurationConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import scribe.thrift.LogEntry;

import com.alibaba.rocketmq.client.exception.MQBrokerException;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.client.producer.DefaultMQProducer;
import com.alibaba.rocketmq.common.message.Message;
import com.alibaba.rocketmq.remoting.exception.RemotingException;
import com.ganji.cateye.flume.AbstractMultiThreadRpcClient;
import com.ganji.cateye.utils.StatsDClientHelper;

/**
 * KestrelRpcClient 保证事务状态
 * @author asdf
 *
 */
public class KestrelRpcClient extends AbstractMultiThreadRpcClient {
	private static final Logger LOGGER = LoggerFactory.getLogger(KestrelRpcClient.class);
	private final Lock stateLock;
	private State connState;
	private String hostname;
	private int port;
	private String name = "";
	private String categoryHeaderKey = null;
	private String forceCategory = null;
	@SuppressWarnings("unused")
	private boolean compress = false;
	private String serializerId = "scribe";
	@SuppressWarnings("unused")
	private final int RETRY_INTERVAL = 1*1000;
	@SuppressWarnings("unused")
	private KestrelSerializer serializer = null;
	
	// private List<ByteBuffer> pendingItems = new LinkedList<ByteBuffer>();

	
//	private int compressMsgBodyOverHowmuch;
//	private String topic;
//	private String producerGroup;
//	public final DefaultMQProducer producer;
	private StatsDClientHelper stats;
	// private final Random random = new Random();
	private KestrelThriftClient client;

	public KestrelRpcClient() {
		stateLock = new ReentrantLock(true);
		connState = State.INIT;
		stats = new StatsDClientHelper();		
	}

	@Override
	public void append(Event event) throws EventDeliveryException {
		try {
			if (!isActive()) {
				throw new EventDeliveryException("Client was closed due to error.  Please create a new client");
			}
			//Message m = new Message("cateye", event.getHeaders().get("category"), event.getBody());
			// producer.send(m);			
			//ByteBuffer buf = serializer.encodeToByteBuffer(msg, false);
			 
			List<ByteBuffer> items = new ArrayList<ByteBuffer>();
			items.add(serializer.encodeToByteBuffer(serializer.serialize(event), compress));
			client.put(forceCategory, items, 0);
			
			stats.incrementCounter("producer", 1);

		} catch (Throwable e) {
			// MQClientException RemotingException MQBrokerException InterruptedException
			if (e instanceof ExecutionException) {
				Throwable cause = e.getCause();
				// if (cause instanceof EventDeliveryException) {
				if (cause instanceof MQClientException
						|| cause instanceof RemotingException
						|| cause instanceof MQBrokerException
						|| cause instanceof InterruptedException) {
					// throw (EventDeliveryException) cause;
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
	public void appendBatch(final List<Event> events) throws EventDeliveryException {
		boolean destroyedClient = false;
		try {
			if (!isActive()) {
				throw new EventDeliveryException("Client was closed due to error.  Please create a new client");
			}
			LOGGER.warn("RocketmqRpcClient: appendBatch size={}", events.size());
//			for (Event event : events) {
//				Message m = new Message("cateye", event.getHeaders().get("category"), event.getBody());
//				producer.send(m);
//			}
			List<ByteBuffer> items = new ArrayList<ByteBuffer>();
			for (Event event : events) {
				items.add(serializer.encodeToByteBuffer(serializer.serialize(event), compress));
			}
			client.put(forceCategory, items, 0);
			
			stats.incrementCounter("producer", events.size());
		} catch (Throwable e) {
			// MQClientException RemotingException MQBrokerException InterruptedException
			LOGGER.warn("RocketmqRpcClient fail to send message", e);
			if (e instanceof ExecutionException) {
				Throwable cause = e.getCause();
				// if (cause instanceof EventDeliveryException) {
				if (cause instanceof MQClientException
						|| cause instanceof RemotingException
						|| cause instanceof MQBrokerException
						|| cause instanceof InterruptedException) {
					// throw (EventDeliveryException) cause;
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
			stats.stop();
			// producer.shutdown();
			client.close();
			
			System.out.println("client close");
		} catch (Throwable ex) {
			if (ex instanceof Error) {
				throw (Error) ex;
			} else if (ex instanceof RuntimeException) {
				throw (RuntimeException) ex;
			}
			throw new FlumeException("Failed to close RPC client. ", ex);
		} finally {
			stateLock.unlock();
		}
	}

	@SuppressWarnings("unused")
	private void dump(Properties properties) {
		for (Object key : properties.keySet()) {
			// System.out.println(properties.getProperty(key.toString()));
			LOGGER.warn("RocketmqRpcClient dump conifg {}={}", key.toString(), properties.getProperty(key.toString()));
		}
	}

	@Override
	protected void configure(Properties properties) throws FlumeException {
		if (isActive()) {
			throw new FlumeException("Attempting to re-configured an already configured client!");
		}
		stateLock.lock();
		try {
			// dump(properties);
			List<HostInfo> hosts = HostInfo.getHostInfoList(properties);
			if (hosts.size() > 0) {
				HostInfo host = hosts.get(0);
				hostname = host.getHostName();
				port = host.getPortNumber();
			} else {
				hostname = properties.getProperty("hostname", "127.0.0.1");
				port = Integer.parseInt(properties.getProperty("port", "9876"));
			}

			forceCategory = properties.getProperty("forceCategory", "");
			if(StringUtils.isEmpty(forceCategory))
				throw new FlumeException("forceCategory of KestrelRpcClient not configed");
			
			compress =  Boolean.parseBoolean(properties.getProperty("compress", "False"));
			
			
			serializerId =  properties.getProperty("serializerId", "scribe");
			if( serializerId.equalsIgnoreCase("scribe")) {
				serializer = new ScribeSerializer();
			}
			else if( serializerId.equalsIgnoreCase("plain-message") ) {
				serializer = new PlainMessageSerializer();
			}
			else 
				throw new RuntimeException("invalid serializer specified");
			categoryHeaderKey = properties.getProperty(KestrelSinkConstants.CONFIG_CATEGORY_HEADER
					, KestrelSinkConstants.CONFIG_CATEGORY_HEADER_DEFAULT);
			
			Context context = new Context();
			context.put("serializerId", serializerId);
			context.put(KestrelSinkConstants.CONFIG_CATEGORY_HEADER, categoryHeaderKey);
			serializer.configure(context);
			
			batchSize = Integer.parseInt(properties.getProperty(
					RpcClientConfigurationConstants.CONFIG_BATCH_SIZE,
					/* RpcClientConfigurationConstants.DEFAULT_BATCH_SIZE.toString() */"50"));
			requestTimeout = Long.parseLong(properties.getProperty(
					RpcClientConfigurationConstants.CONFIG_REQUEST_TIMEOUT,
					String.valueOf(RpcClientConfigurationConstants.DEFAULT_REQUEST_TIMEOUT_MILLIS)));
			if (requestTimeout < 1000) {
				LOGGER.warn("Request timeout specified less than 1s. Using default value instead.");
				requestTimeout = RpcClientConfigurationConstants.DEFAULT_REQUEST_TIMEOUT_MILLIS;
			}

			client = new KestrelThriftClient(hostname, port);
			name = String.format("%d@%s:%d", new Random().nextInt(), hostname, port);
			
			connState = State.READY;
		} catch (Throwable ex) {
			LOGGER.warn("RocketmqRpcClient fail to start producer");
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

	// rocketmq producor api不支持批量， 所有这里是一个awful实现

	private static enum State {
		INIT, READY, DEAD
	}

	public boolean equals(Object o) {
		if (o == null || !(o instanceof KestrelRpcClient)) {
			return false;
		}
		KestrelRpcClient r = (KestrelRpcClient) o;
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
