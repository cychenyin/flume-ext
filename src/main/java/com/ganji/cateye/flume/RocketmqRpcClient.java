package com.ganji.cateye.flume;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.Random;

import org.apache.flume.Event;
import org.apache.flume.EventDeliveryException;
import org.apache.flume.FlumeException;
import org.apache.flume.api.AbstractRpcClient;
import org.apache.flume.api.HostInfo;
import org.apache.flume.api.RpcClientConfigurationConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.rocketmq.client.exception.MQBrokerException;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.client.producer.DefaultMQProducer;
import com.alibaba.rocketmq.common.message.Message;
import com.alibaba.rocketmq.remoting.exception.RemotingException;
import com.ganji.cateye.utils.StatsDClientHelper;

public class RocketmqRpcClient extends AbstractRpcClient {
	private static final Logger LOGGER = LoggerFactory.getLogger(RocketmqRpcClient.class);
	// private int batchSize;
	private long requestTimeout;
	private final Lock stateLock;
	private State connState;
	private String hostname;
	private int port;
	private int compressMsgBodyOverHowmuch;
	private ConnectionPoolManager connectionManager;
	private final ExecutorService callTimeoutPool;
	private final AtomicLong threadCounter;
	private int connectionPoolSize;
	StatsDClientHelper stats;
	// private final Random random = new Random();

	public RocketmqRpcClient() {
		stateLock = new ReentrantLock(true);
		connState = State.INIT;
		stats = new StatsDClientHelper();
		threadCounter = new AtomicLong(0);
		// OK to use cached threadpool, because this is simply meant to timeout the calls - and is IO bound.
		callTimeoutPool = Executors.newCachedThreadPool(new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				t.setName("Flume Thrift RPC thread - " + String.valueOf(threadCounter.incrementAndGet()));
				LOGGER.warn("RocketmqRpcClient: sink add new thread. name=" + t.getName());
				return t;
			}
		});
	}

	@Override
	public void append(Event event) throws EventDeliveryException {
		ClientWrapper client = null;
		boolean destroyedClient = false;
		try {
			if (!isActive()) {
				throw new EventDeliveryException("Client was closed due to error.  Please create a new client");
			}
			client = connectionManager.checkout();
			
			doAppend(client, event).get(requestTimeout, TimeUnit.MILLISECONDS);
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
			destroyedClient = true;
			// If destroy throws, we still don't want to reuse the client, so mark it as destroyed before we actually do.
			if (client != null) {
				connectionManager.destroy(client);
			}
			if (e instanceof Error) {
				throw (Error) e;
			} else if (e instanceof RuntimeException) {
				throw (RuntimeException) e;
			}
			throw new EventDeliveryException("Failed to send event. ", e);
		} finally {
			if (client != null && !destroyedClient) {
				connectionManager.checkIn(client);
			}
		}
	}

	@Override
	public void appendBatch(List<Event> events) throws EventDeliveryException {
		ClientWrapper client = null;
		boolean destroyedClient = false;
		try {
			if (!isActive()) {
				throw new EventDeliveryException("Client was closed due to error.  Please create a new client");
			}
			LOGGER.warn("RocketmqRpcClient: appendBatch size={}", events.size());
			client = connectionManager.checkout();
//			for (Event event : events) {
//				Message m = new Message("cateye", event.getHeaders().get("category"), event.getBody());
//				doAppend(client, m).get(requestTimeout, TimeUnit.MILLISECONDS);
//			}
			doAppendBatch(client, events);
			stats.incrementCounter("producer", events.size());
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
			destroyedClient = true;
			// If destroy throws, we still don't want to reuse the client, so mark it as destroyed before we actually do.
			if (client != null) {
				connectionManager.destroy(client);
			}
			if (e instanceof Error) {
				throw (Error) e;
			} else if (e instanceof RuntimeException) {
				throw (RuntimeException) e;
			}
			throw new EventDeliveryException("Failed to send event. ", e);
		} finally {
			if (client != null && !destroyedClient) {
				connectionManager.checkIn(client);
			}
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
			connectionManager.closeAll();
			callTimeoutPool.shutdown();
			if (!callTimeoutPool.awaitTermination(5, TimeUnit.SECONDS)) {
				callTimeoutPool.shutdownNow();
			}
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
			HostInfo host = HostInfo.getHostInfoList(properties).get(0);
			hostname = host.getHostName();
			port = host.getPortNumber();

			LOGGER.warn("===========RocketmqRpcClient: hostname={} port={}", this.hostname, this.port);
			compressMsgBodyOverHowmuch = Integer.parseInt(properties.getProperty(
					"compress-msg-body-over-how-much",
					String.valueOf(4000)));

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
			connectionPoolSize = Integer.parseInt(properties.getProperty(
					RpcClientConfigurationConstants.CONFIG_CONNECTION_POOL_SIZE,
					String.valueOf(RpcClientConfigurationConstants.DEFAULT_CONNECTION_POOL_SIZE)));
			if (connectionPoolSize < 1) {
				LOGGER.warn("Connection Pool Size specified is less than 1. Using default value instead.");
				connectionPoolSize = RpcClientConfigurationConstants.DEFAULT_CONNECTION_POOL_SIZE;
			}
			connectionManager = new ConnectionPoolManager(connectionPoolSize);
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

	private Future<Void> doAppend(final ClientWrapper client, final Event event) throws Exception {
		return callTimeoutPool.submit(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				Message m = new Message("cateye", event.getHeaders().get("category"), event.getBody());
				client.producer.send(m);
				// 如果send有异常，则让它自然pop；否则就是成功了；这里不强制所有的状态就绪；原因详见rocketmq的发送代码注释
				// SendResult status = client.producer.send(m);
				// if (status.getSendStatus() != SendStatus.SEND_OK) {
				// throw new EventDeliveryException("Failed to deliver events. Server " +
				// "returned status : " + status.getSendStatus().name());
				// }
				return null;
			}
		});
	}

	// rocketmq producor api不支持批量， 所有这里是一个awful实现
	private Future<Void> doAppendBatch(final ClientWrapper client, final List<Event> events) throws Exception {

		return callTimeoutPool.submit(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				for (Event event : events) {
					Message m = new Message("cateye", event.getHeaders().get("category"), event.getBody());
					client.producer.send(m);
				}
				return null;
			}
		});
	}

	private static enum State {
		INIT, READY, DEAD
	}

	/**
	 * Wrapper around a client and transport, so we can clean up when this client gets closed.
	 */
	private class ClientWrapper {
		public final DefaultMQProducer producer;

		private final int hashCode;

		public ClientWrapper() throws Exception {
			producer = new DefaultMQProducer("cateye");
			producer.setCreateTopicKey("cateye");
			producer.setProducerGroup("cateye");
			producer.setNamesrvAddr("127.0.0.1:9876");
			// producer.setNamesrvAddr(String.format("{}:{}", RocketmqRpcClient.this.hostname, RocketmqRpcClient.this.port));
			producer.setCompressMsgBodyOverHowmuch(RocketmqRpcClient.this.compressMsgBodyOverHowmuch);
			producer.setInstanceName("cateye" + (new Random()).nextInt());
			producer.start();
			
			LOGGER.warn("RocketmqRpcClient getCreateTopicKey={} instanceName={}", producer.getCreateTopicKey(), producer.getInstanceName());
			hashCode = producer.hashCode();
		}

		public boolean equals(Object o) {
			if (o == null) {
				return false;
			}
			// Since there is only one wrapper with any given client, direct comparison is good enough.
			if (this == o) {
				return true;
			}
			return false;
		}

		public int hashCode() {
			return hashCode;
		}

	}

	private class ConnectionPoolManager {
		private final Queue<ClientWrapper> availableClients;
		private final Set<ClientWrapper> checkedOutClients;
		private final int maxPoolSize;
		private int currentPoolSize;
		private final Lock poolLock;
		private final Condition availableClientsCondition;

		public ConnectionPoolManager(int poolSize) {
			this.maxPoolSize = poolSize;
			availableClients = new LinkedList<ClientWrapper>();
			checkedOutClients = new HashSet<ClientWrapper>();
			poolLock = new ReentrantLock();
			availableClientsCondition = poolLock.newCondition();
			currentPoolSize = 0;
		}

		public ClientWrapper checkout() throws Exception {
			ClientWrapper ret = null;
			poolLock.lock();
			try {
				if (availableClients.isEmpty() && currentPoolSize < maxPoolSize) {
					ret = new ClientWrapper();
					currentPoolSize++;
					checkedOutClients.add(ret);
					LOGGER.warn("RocketmqRpcClient add new rocketmq client. total=" + currentPoolSize);
					return ret;
				}
				while (availableClients.isEmpty()) {
					availableClientsCondition.await();
				}
				ret = availableClients.poll();
				checkedOutClients.add(ret);
			} finally {
				poolLock.unlock();
			}
			return ret;
		}

		public void checkIn(ClientWrapper client) {
			poolLock.lock();
			try {
				availableClients.add(client);
				checkedOutClients.remove(client);
				availableClientsCondition.signal();
			} finally {
				poolLock.unlock();
			}
		}

		public void destroy(ClientWrapper client) {
			poolLock.lock();
			try {
				checkedOutClients.remove(client);
				currentPoolSize--;
				LOGGER.warn("RocketmqRpcClient remove rocketmq client. total=" + currentPoolSize);
			} finally {
				poolLock.unlock();
			}
			client.producer.shutdown();
		}

		public void closeAll() {
			poolLock.lock();
			try {
				for (ClientWrapper c : availableClients) {
					c.producer.shutdown();
					currentPoolSize--;
				}
				/*
				 * Be cruel and close even the checked out clients. The threads writing using these will now get an exception.
				 */
				for (ClientWrapper c : checkedOutClients) {
					c.producer.shutdown();
					currentPoolSize--;
				}
			} finally {
				poolLock.unlock();
			}
		}
	}
}
