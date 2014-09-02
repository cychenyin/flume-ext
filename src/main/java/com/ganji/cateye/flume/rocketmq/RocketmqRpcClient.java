package com.ganji.cateye.flume.rocketmq;

import java.util.List;
import java.util.Properties;
import java.util.Random;

import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.FlumeException;
import org.apache.flume.api.HostInfo;
import org.apache.flume.api.RpcClientConfigurationConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.rocketmq.client.producer.DefaultMQProducer;
import com.alibaba.rocketmq.common.message.Message;
import com.ganji.cateye.flume.AbstractMultiThreadRpcClient;
import com.ganji.cateye.flume.MessageSerializer;
import com.ganji.cateye.flume.PlainMessageSerializer;
import com.ganji.cateye.flume.ScribeSerializer;


/**
 * RocketmqRpcClient
 * 
 * @author asdf
 * 
 */
public class RocketmqRpcClient extends AbstractMultiThreadRpcClient {
	private static final Logger LOGGER = LoggerFactory.getLogger(RocketmqRpcClient.class);
	private String hostname;
	private int port;
	private int compressMsgBodyOverHowmuch;
	private String topic;
	private String producerGroup;
	public DefaultMQProducer producer;
	public String categoryHeaderKey;
	private MessageSerializer serializer;

	public RocketmqRpcClient() {
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
				hostname = properties.getProperty(RocketmqSinkConsts.CONFIG_HOSTNAME, RocketmqSinkConsts.DEFAULT_HOSTNAME);
				port = Integer.parseInt(properties
						.getProperty(RocketmqSinkConsts.CONFIG_PORT, RocketmqSinkConsts.DEFAULT_PORT));
			}

			// serialization
			String serializerName = properties.getProperty(RocketmqSinkConsts.CONFIG_SERIALIZER,
					RocketmqSinkConsts.DEFAULT_SERIALIZER);
			if (serializerName.equalsIgnoreCase(RocketmqSinkConsts.DEFAULT_SERIALIZER)) {
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
			this.categoryHeaderKey = properties.getProperty(RocketmqSinkConsts.CONFIG_CATEGORY_HEADER_KEY,
					RocketmqSinkConsts.DEFAULT_CATEGORY_HEADER_KEY);
			Context context = new Context();
			context.put(RocketmqSinkConsts.CONFIG_CATEGORY_HEADER_KEY, categoryHeaderKey);
			serializer.configure(context);

			compressMsgBodyOverHowmuch = Integer.parseInt(properties.getProperty(
					RocketmqSinkConsts.CONFIG_COMPRESSMSGBODYOVERHOWMUCH,
					String.valueOf(RocketmqSinkConsts.DEFAULT_COMPRESSMSGBODYOVERHOWMUCH)));

			batchSize = Integer.parseInt(properties.getProperty(
					RocketmqSinkConsts.CONFIG_BATCHSIZE,
					RocketmqSinkConsts.DEFAULT_BATCH_SIZE));

			requestTimeout = Long.parseLong(properties.getProperty(
					RpcClientConfigurationConstants.CONFIG_REQUEST_TIMEOUT,
					String.valueOf(RpcClientConfigurationConstants.DEFAULT_REQUEST_TIMEOUT_MILLIS)));
			if (requestTimeout < 1000) {
				LOGGER.warn("Request timeout specified less than 1s. Using default value instead.");
				requestTimeout = RpcClientConfigurationConstants.DEFAULT_REQUEST_TIMEOUT_MILLIS;
			}
			// producer
			topic = properties.getProperty(RocketmqSinkConsts.CONFIG_TOPIC, RocketmqSinkConsts.DEFAULT_TOPIC);
			producerGroup = properties.getProperty(RocketmqSinkConsts.CONFIG_PRODUCERGROUP,
					RocketmqSinkConsts.DEFAULT_PRODUCERGROUP);

			producer = new DefaultMQProducer(RocketmqSinkConsts.DEFAULT_PRODUCERGROUP);
			producer.setCreateTopicKey(topic);
			producer.setProducerGroup(producerGroup);
			// producer.setNamesrvAddr("127.0.0.1:9876");
			producer.setNamesrvAddr(String.format("%s:%d", hostname, port));
			producer.setCompressMsgBodyOverHowmuch(compressMsgBodyOverHowmuch);
			producer.setInstanceName(producerGroup + "_" + (new Random()).nextInt());

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

	@Override
	public void close() throws FlumeException {
		try {
			// Do not release this, because this client is not to be used again
			stateLock.lock();
			connState = State.DEAD;
			
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

	// rocketmq producor api不支持批量， 所有这里是一个awful实现
	@Override
	public void doAppendBatch(List<Event> events) throws Exception {
		for (Event event : events) {
			Message m = new Message(topic, event.getHeaders().get(categoryHeaderKey), event.getBody());
			producer.send(m);
		}
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
