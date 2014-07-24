/*
 * create: asdf
 * create datetime: 2014-07-24 16:17:16 
 * 
 * */
package com.ganji.cateye.flume;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.flume.Channel;
import org.apache.flume.ChannelException;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.EventDeliveryException;
import org.apache.flume.Transaction;
import org.apache.flume.api.RpcClient;
import org.apache.flume.api.RpcClientConfigurationConstants;
import org.apache.flume.conf.Configurable;
import org.apache.flume.instrumentation.SinkCounter;
import org.apache.flume.sink.AbstractSink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public abstract class AbstractMultiThreadRpcSink extends AbstractSink implements Configurable {

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractMultiThreadRpcSink.class);
	private String hostname;
	private Integer port;
	private Properties clientProps;
	private SinkCounter sinkCounter;
	private final ExecutorService callTimeoutPool = Executors.newCachedThreadPool(new ThreadFactory() {
		@Override
		public Thread newThread(Runnable r) {
			Thread t = new Thread(r);
			t.setName("AbstractMultiThreadRpcSink thread - " + String.valueOf(threadCounter.incrementAndGet()));
			LOGGER.warn("AbstractMultiThreadRpcSink: add new thread. name=" + t.getName());
			return t;
		}
	});
	private final AtomicLong threadCounter = new AtomicLong(0);
	private int connectionPoolSize = 1;
	private ConnectionPoolManager connectionManager = new ConnectionPoolManager(connectionPoolSize);

	@Override
	public void configure(Context context) {
		clientProps = new Properties();

		hostname = context.getString("hostname");
		port = context.getInteger("port");

		Preconditions.checkState(hostname != null, "No hostname specified");
		Preconditions.checkState(port != null, "No port specified");

		clientProps.setProperty(RpcClientConfigurationConstants.CONFIG_HOSTS, "h1");
		clientProps.setProperty(RpcClientConfigurationConstants.CONFIG_HOSTS_PREFIX +
				"h1", hostname + ":" + port);

		for (Entry<String, String> entry : context.getParameters().entrySet()) {
			clientProps.setProperty(entry.getKey(), entry.getValue());
		}

		if (sinkCounter == null) {
			sinkCounter = new SinkCounter(getName());
		}
	
		connectionPoolSize = Integer.parseInt(clientProps.getProperty(
				RpcClientConfigurationConstants.CONFIG_CONNECTION_POOL_SIZE,
				String.valueOf(RpcClientConfigurationConstants.DEFAULT_CONNECTION_POOL_SIZE)));
		if (connectionPoolSize < 1) {
			LOGGER.warn("Connection Pool Size specified is less than 1. Using default value instead.");
			connectionPoolSize = RpcClientConfigurationConstants.DEFAULT_CONNECTION_POOL_SIZE;
		}
		this.connectionManager.setPoolSize(connectionPoolSize);
	}

	/**
	 * Returns a new {@linkplain RpcClient} instance configured using the given {@linkplain Properties} object. This method is called
	 * whenever a new connection needs to be created to the next hop.
	 * 
	 * @param props
	 * @return
	 */
	// protected abstract RpcClient initializeRpcClient(Properties props);
	protected abstract AbstractMultiThreadRpcClient initializeRpcClient(Properties props);

	/**
	 * The start() of RpcSink is more of an optimization that allows connection to be created before the process() loop is started. In case
	 * it so happens that the start failed, the process() loop will itself attempt to reconnect as necessary. This is the expected behavior
	 * since it is possible that the downstream source becomes unavailable in the middle of the process loop and the sink will have to retry
	 * the connection again.
	 */
	@Override
	public void start() {
		LOGGER.info("MultiThread Rpc Starting {}...", this);
		sinkCounter.start();
		super.start();
		LOGGER.info("MultiThread Rpc sink {} started.", getName());
	}

	@Override
	public void stop() {
		LOGGER.info("MultiThread Rpc sink {} stopping...", getName());

		if (connectionManager != null)
			connectionManager.closeAll();
			System.out.println(connectionManager.currentPoolSize);
		try {
			callTimeoutPool.shutdown();
			if (!callTimeoutPool.awaitTermination(2, TimeUnit.SECONDS)) {
				callTimeoutPool.shutdownNow();
				System.out.println("shutdownNow");
			}
			System.out.println("shutdown end");
		} catch (Exception ex) {
			LOGGER.error("MultiThread Rpc sink Interrupted while waiting for connection reset executor to shut down");
		}
		
		if (sinkCounter != null)
			sinkCounter.stop();

		super.stop();

		LOGGER.info("MultiThread Rpc sink {} stopped. Metrics: {}", getName(), sinkCounter);
	}

	@Override
	public String toString() {
		return "RpcSink " + getName() + " { host: " + hostname + ", port: " + port + " }";
	}
	
	// the java api now not support future listener register, so, between exactly result and multi thread model, my choice is multi thread.
	// but in partial do process can return the result of previous tranasaction; and which one? who care!
	private volatile Status unreliableStatus;

	private Status doProcess() throws EventDeliveryException {
		Status status = Status.READY;
		Channel channel = getChannel();
		Transaction transaction = channel.getTransaction();

		AbstractMultiThreadRpcClient client = null;
		try {
			client = connectionManager.checkout();
			transaction.begin();

			List<Event> batch = Lists.newLinkedList();

			for (int i = 0; i < client.getBatchSize(); i++) {
				Event event = channel.take();

				if (event == null) {
					break;
				}

				batch.add(event);
			}

			int size = batch.size();
			int batchSize = client.getBatchSize();

			if (size == 0) {
				sinkCounter.incrementBatchEmptyCount();
				status = Status.BACKOFF;
			} else {
				if (size < batchSize) {
					sinkCounter.incrementBatchUnderflowCount();
				} else {
					sinkCounter.incrementBatchCompleteCount();
				}
				sinkCounter.addToEventDrainAttemptCount(size);
				client.appendBatch(batch);
			}

			transaction.commit();
			sinkCounter.addToEventDrainSuccessCount(size);

		} catch (Throwable t) {
			transaction.rollback();
			if (t instanceof Error) {
				throw (Error) t;
			} else if (t instanceof ChannelException) {
				LOGGER.error("Rpc Sink " + getName() + ": Unable to get event from" +
						" channel " + channel.getName() + ". Exception follows.", t);
				status = Status.BACKOFF;
			} else {
				// destroyConnection();
				if (client != null)
					this.connectionManager.destroy(client);
				throw new EventDeliveryException("Failed to send events", t);
			}
		} finally {
			transaction.close();
			if (client != null) {
				this.connectionManager.checkIn(client);
			}
		}
		unreliableStatus = status;
		return status;
	}

	@Override
	public Status process() throws EventDeliveryException {
		// By the way, future api in java seems quite stupid.
		callTimeoutPool.submit(new Callable<Status>() {
			@Override
			public Status call() throws Exception {
				return doProcess();
			}
		});
		return unreliableStatus;
	}

	private class ConnectionPoolManager {
		private final Queue<AbstractMultiThreadRpcClient> availableClients;
		private final Set<AbstractMultiThreadRpcClient> checkedOutClients;
		private int currentPoolSize;
		private int maxPoolSize;
		private final Lock poolLock;
		private final Condition availableClientsCondition;

		public ConnectionPoolManager(int poolSize) {
			this.maxPoolSize = poolSize;
			availableClients = new LinkedList<AbstractMultiThreadRpcClient>();
			checkedOutClients = new HashSet<AbstractMultiThreadRpcClient>();
			poolLock = new ReentrantLock();
			availableClientsCondition = poolLock.newCondition();
			currentPoolSize = 0;
		}
		public int currentPoolSize(){
			return  availableClients.size() + checkedOutClients.size();
		}
		public AbstractMultiThreadRpcClient checkout() throws Exception {
			AbstractMultiThreadRpcClient ret = null;
			poolLock.lock();
			try {
				if (availableClients.isEmpty() && currentPoolSize() < maxPoolSize) {
					ret = initializeRpcClient(clientProps);
					// currentPoolSize++;
					checkedOutClients.add(ret);
					LOGGER.warn("RocketmqRpcClient add new rocketmq client. ={},maxPoolSize={}", currentPoolSize, maxPoolSize);
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

		public void checkIn(AbstractMultiThreadRpcClient client) {
			poolLock.lock();
			try {
				availableClients.add(client);
				checkedOutClients.remove(client);
				availableClientsCondition.signal();
			} finally {
				poolLock.unlock();
			}
		}

		public void destroy(AbstractMultiThreadRpcClient client) {
			poolLock.lock();
			try {
				LOGGER.warn("RocketmqRpcClient remove rocketmq client id={}. currentPoolSize={}", client.getName(), currentPoolSize());
				checkedOutClients.remove(client);
				// currentPoolSize--;
			} finally {
				poolLock.unlock();
			}
			client.close();
		}

		public void closeAll() {
			poolLock.lock();
			try {
				for (AbstractMultiThreadRpcClient c : availableClients) {
					c.close();
					// currentPoolSize--;
				}
				/*
				 * Be cruel and close even the checked out clients. The threads writing using these will now get an exception.
				 */
				for (AbstractMultiThreadRpcClient c : checkedOutClients) {
					c.close();
					// currentPoolSize--;
				}
			} finally {
				poolLock.unlock();
			}
		}

		public void setPoolSize(int size) {
			if (size < 1)
				size = 1;
			this.maxPoolSize = currentPoolSize > size ? currentPoolSize : size;
		}
	}

}
