package com.ganji.cateye.flume.rocketmq;

import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.EventDeliveryException;
import org.apache.flume.FlumeException;
import org.apache.flume.api.HostInfo;
import org.apache.flume.api.RpcClientConfigurationConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.rocketmq.client.exception.MQBrokerException;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.client.producer.DefaultMQProducer;
import com.alibaba.rocketmq.common.message.Message;
import com.alibaba.rocketmq.remoting.exception.RemotingException;
import com.ganji.cateye.flume.AbstractMultiThreadRpcClient;
import com.ganji.cateye.flume.MessageSerializer;
import com.ganji.cateye.flume.PlainMessageSerializer;
import com.ganji.cateye.flume.ScribeSerializer;
import com.ganji.cateye.utils.StatsDClientHelper;

public class RocketmqRpcClient extends AbstractMultiThreadRpcClient {
	private static final Logger LOGGER = LoggerFactory.getLogger(RocketmqRpcClient.class);
	private final Lock stateLock;
	private State connState;
	private String hostname;
	private int port;
	private int compressMsgBodyOverHowmuch;
	private String topic;
	private String producerGroup;
	private StatsDClientHelper stats;
	// private final Random random = new Random();
	public final DefaultMQProducer producer;
	public String categoryHeaderKey;

	MessageSerializer serializer;
	

	public RocketmqRpcClient() {
		stateLock = new ReentrantLock(true);
		connState = State.INIT;
		stats = new StatsDClientHelper();

		producer = new DefaultMQProducer(RocketmqSinkConstants.CONFIG_PRODUCERGROUP_DEFAULT);
		serializer = new ScribeSerializer();
	}

	@Override
	public void append(Event event) throws EventDeliveryException {
		try {
			if (!isActive()) {
				throw new EventDeliveryException("Client was closed due to error.  Please create a new client");
			}

			Message m = new Message(topic, event.getHeaders().get(categoryHeaderKey), event.getBody());
			producer.send(m);
			// 如果send有异常，则让它自然pop；否则就是成功了；这里不强制所有的状态就绪；原因详见rocketmq的发送代码注释
			// SendResult status = client.producer.send(m);
			// if (status.getSendStatus() != SendStatus.SEND_OK) {
			// throw new EventDeliveryException("Failed to deliver events. Server " +
			// "returned status : " + status.getSendStatus().name());
			// }
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
			for (Event event : events) {
				Message m = new Message(topic, event.getHeaders().get(categoryHeaderKey), event.getBody());
				producer.send(m);
			}
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
			producer.shutdown();
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
				hostname = properties.getProperty(RocketmqSinkConstants.CONFIG_HOSTNAME, RocketmqSinkConstants.CONFIG_HOSTNAME_DEFAULT);
				port = Integer.parseInt(properties
						.getProperty(RocketmqSinkConstants.CONFIG_PORT, RocketmqSinkConstants.CONFIG_PORT_DEFAULT));
			}

			// serialization
			String serializerId = properties.getProperty(RocketmqSinkConstants.CONFIG_SERIALIZERID,
					RocketmqSinkConstants.CONFIG_SERIALIZERID_DEFAULT);
			if (serializerId.equalsIgnoreCase(RocketmqSinkConstants.CONFIG_SERIALIZERID_DEFAULT)) {
				serializer = new ScribeSerializer();
			}
			else if (serializerId.equalsIgnoreCase("plain-message")) {
				serializer = new PlainMessageSerializer();
			}
			else
				throw new RuntimeException("invalid serializer specified");
			this.categoryHeaderKey = properties.getProperty(RocketmqSinkConstants.CONFIG_CATEGORY_HEADER,
					RocketmqSinkConstants.CONFIG_CATEGORY_HEADER_DEFAULT);
			Context context = new Context();
			context.put(RocketmqSinkConstants.CONFIG_CATEGORY_HEADER, categoryHeaderKey);
			serializer.configure(context);
			// producer
			topic = properties.getProperty(RocketmqSinkConstants.CONFIG_TOPIC, RocketmqSinkConstants.CONFIG_TOPIC_DEFAULT);
			producerGroup = properties.getProperty(RocketmqSinkConstants.CONFIG_PRODUCERGROUP,
					RocketmqSinkConstants.CONFIG_PRODUCERGROUP_DEFAULT);

			compressMsgBodyOverHowmuch = Integer.parseInt(properties.getProperty(
					RocketmqSinkConstants.CONFIG_COMPRESSMSGBODYOVERHOWMUCH,
					String.valueOf(RocketmqSinkConstants.CONFIG_COMPRESSMSGBODYOVERHOWMUCH_DEFAULT)));

			batchSize = Integer.parseInt(properties.getProperty(
					RpcClientConfigurationConstants.CONFIG_BATCH_SIZE,
					RocketmqSinkConstants.DEFAULT_BATCH_SIZE));

			requestTimeout = Long.parseLong(properties.getProperty(
					RpcClientConfigurationConstants.CONFIG_REQUEST_TIMEOUT,
					String.valueOf(RpcClientConfigurationConstants.DEFAULT_REQUEST_TIMEOUT_MILLIS)));
			if (requestTimeout < 1000) {
				LOGGER.warn("Request timeout specified less than 1s. Using default value instead.");
				requestTimeout = RpcClientConfigurationConstants.DEFAULT_REQUEST_TIMEOUT_MILLIS;
			}

			producer.setCreateTopicKey(topic);
			producer.setProducerGroup(producerGroup);
			// producer.setNamesrvAddr("127.0.0.1:9876");
			producer.setNamesrvAddr(String.format("%s:%d", hostname, port));
			producer.setCompressMsgBodyOverHowmuch(compressMsgBodyOverHowmuch);
			producer.setInstanceName(producerGroup + "_" + (new Random()).nextInt());

			LOGGER.warn("===========RocketmqRpcClient: hostname={} port={} /={}", this.hostname, this.port,
					String.format("{}:{}", hostname, port));
			LOGGER.warn("RocketmqRpcClient getCreateTopicKey={} instanceName={} clientId={}", producer.getCreateTopicKey(),
					producer.getInstanceName(), producer.buildMQClientId());

			producer.start();

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
		if (o == null || !(o instanceof RocketmqRpcClient)) {
			return false;
		}
		RocketmqRpcClient r = (RocketmqRpcClient) o;
		if (r.producer != null && this.producer.buildMQClientId().equals(r.producer.buildMQClientId())) {
			return true;
		}
		return false;
	}

	public int hashCode() {
		return producer.buildMQClientId().hashCode();
	}

	@Override
	public String getName() {
		return this.producer.buildMQClientId();
	}
}
