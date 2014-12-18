/*
 * create: asdf
 * create datetime: 2014-07-24 16:17:16 
 * 
 * */
package com.ganji.cateye.flume;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
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
import org.apache.flume.channel.file.FileChannel;
import org.apache.flume.conf.Configurable;
import org.apache.flume.instrumentation.SinkCounter;
import org.apache.flume.sink.AbstractSink;
import org.apache.flume.source.avro.AvroSourceProtocol.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ganji.cateye.utils.StatsDClientHelper;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.ganji.cateye.flume.AbstractMultiThreadRpcClient;

/**
 * 远程RPC抽象类，封装了远程数据访问的多线程和连接池细节。 注意： 该抽象类能高效工作的前提是时候用的channel在不同线程中返回不同的transaction实例。具体所配参考
 * channel实现中getTransaction方法和createTransaction方法的机制。
 * 
 * @author asdf
 * 
 * 增加accurate_process_status=true(默认true)参数支持, 用于failover和loadbalance场景的支持，会比false情况慢一点点
 * update by chenyin 2014-11-28 14:23:57
 * eg.
 * agent.sinks.k2.type = com.ganji.cateye.flume.scribe.ScribeSink
 * agent.sinks.k2.channel = c1
 * agent.sinks.k2.hostname = localhost
 * agent.sinks.k2.port = 51463
 * agent.sinks.k2.batchSize = 50
 * agent.sinks.k2.maxConnections = 2 
 * agent.sinks.k2.scribe.category.header = category
 * agent.sinks.k2.accurate_process_status = true
 */
public abstract class AbstractMultiThreadRpcSink extends AbstractSink implements Configurable {

	private static final Logger logger = LoggerFactory.getLogger(AbstractMultiThreadRpcSink.class);
	private String hostname;
	private Integer port;
	private Properties clientProps;
	private SinkCounter sinkCounter;
	// private ExecutorService callTimeoutPoolx;
	private ThreadPoolExecutor callTimeoutPool;

	private ListeningExecutorService listeningPool;

	private final AtomicLong threadCounter = new AtomicLong(0);
	private int connectionPoolSize = 1;
	private ConnectionPoolManager connectionManager = new ConnectionPoolManager(connectionPoolSize);
	private StatsDClientHelper stats;
	private boolean accurateProcessStatus = false;

	@Override
	public void configure(Context context) {
		stats = new StatsDClientHelper();
		hostname = context.getString("hostname");
		port = context.getInteger("port");

		Preconditions.checkState(hostname != null, "No hostname specified");
		Preconditions.checkState(port != null, "No port specified");

		clientProps = new Properties();
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
			logger.info("Connection Pool Size specified is less than 1. Using default value instead.");
			connectionPoolSize = RpcClientConfigurationConstants.DEFAULT_CONNECTION_POOL_SIZE;
		}
		this.connectionManager.setPoolSize(connectionPoolSize);
		logger.info("{} connectionManager.connectionPoolSize={}", getName(), connectionPoolSize);

		this.accurateProcessStatus = "true".equals(clientProps.getProperty(
				SinkConsts.CONFIG_ACCURATE_PROCESS_STATUS,
				String.valueOf(SinkConsts.DEFAULT_ACCURATE_PROCESS_STATUS)));
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
		logger.info("MultiThread Rpc Starting {}...", this);
		callTimeoutPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(this.connectionPoolSize, new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				t.setName(AbstractMultiThreadRpcSink.this.getName() + "_Thread_" + String.valueOf(threadCounter.incrementAndGet()));
				logger.info("{} add new thread. thread name={}", getName(), t.getName());
				return t;
			}
		});
		this.listeningPool = MoreExecutors.listeningDecorator(callTimeoutPool);
		sinkCounter.start();
		super.start();
		logger.info("MultiThread Rpc sink {} started.", getName());
	}

	@Override
	public void stop() {
		logger.info("MultiThread Rpc sink {} stopping...", getName());

		logger.debug("stop 1");
		try {
			callTimeoutPool.shutdown();
			listeningPool.shutdown();
			logger.debug("stop 1.1");

			if (!callTimeoutPool.awaitTermination(SinkConsts.THREADPOOL_AWAITTERMINATION_TIMEOUT, TimeUnit.SECONDS)) {
				logger.debug("stop 1.2");
				callTimeoutPool.shutdownNow();
				logger.debug("stop 1.3");
			}
		} catch (Exception ex) {
			logger.error("MultiThread Rpc sink interrupted while waiting for connection reset executor to shut down");
		}
		logger.debug("stop 2");
		if (connectionManager != null)
			connectionManager.closeAll();
		if (sinkCounter != null)
			sinkCounter.stop();
		logger.debug("stop 3");
		super.stop();
		logger.debug("stop 4");
		logger.info("MultiThread Rpc sink {} stopped. Metrics: {}", getName(), sinkCounter);
	}

	@Override
	public String toString() {
		return "RpcSink " + getName() + " { host: " + hostname + ", port: " + port + " }";
	}

	// the java api now not support future listener register, so, between exactly result and multi thread model, my choice is multi thread.
	// but in partial do process can return the result of previous tranasaction; and which one? who care!
	private volatile ProcessResult lastProcessResult = ProcessResult.READY;

	private enum ProcessResult {
		READY, BACKOFF, FAIL
	}

	protected ProcessResult doProcess() throws EventDeliveryException {
		ProcessResult result = ProcessResult.READY;
		Channel channel = getChannel();
		if(channel instanceof FileChannel) {
			FileChannel f = (FileChannel)channel;
			// 如果有多个client同时发送的话，channel有可能被别的取光后关闭了
			if(f.isOpen() == false) {
				logger.warn(String.format("Rpc Sink %s 's file channel %s is closed."
						, getName(), channel.getName()));
				return ProcessResult.BACKOFF;
			}
		}
		// 注意， 在同一个线程中， channel.getTransaction()多次调用返回相同的示例；不同线程返回不同的
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
				result = ProcessResult.BACKOFF;
			} else {
				if (size < batchSize) {
					sinkCounter.incrementBatchUnderflowCount();
				} else {
					sinkCounter.incrementBatchCompleteCount();
				}
				sinkCounter.addToEventDrainAttemptCount(size);
				client.appendBatch(batch);
				logger.info("{} rpc sink send successfully. size={}", getName(), batch.size());
			}

			transaction.commit();
			stats.incrementCounter(getName() + ".commit", size);
			sinkCounter.addToEventDrainSuccessCount(size);

		} catch (Throwable t) {
			transaction.rollback();
			stats.incrementCounter(getName() + ".rollbacktimes", 1);
			// 因为在线程内容部，所以吃掉所有的异常
			if (t instanceof Error) {
				logger.error(String.format("Rpc Sink %s fail to send event, client=%s", getName(), client.getName()), t);
				// throw (Error) t;
				result = ProcessResult.FAIL;
			} else if (t instanceof ChannelException) {
				logger.warn(
						String.format("Rpc Sink %s Unable to get event from channel %s. Exception follows.", getName(), channel.getName()),
						t);
				result = ProcessResult.BACKOFF;
			} else {
				// 这种情况下可能是Client出问题导致的，销毁当前client
				logger.warn(String.format("Rpc Sink %s fail to send event, client=%s", getName(), client.getName()), t);
				this.connectionManager.destroy(client);
				client = null;
				// throw new EventDeliveryException("Failed to send events. ", t);
				result = ProcessResult.FAIL;
			}

		} finally {
			transaction.close();
			// 如果已经destroy，已经没必要再做checkin
			if (client != null) {
				this.connectionManager.checkIn(client);
			}
		}
		// logger.debug("doProcess result=" + result);
		lastProcessResult = result;
		return result;
	}

	// return last process result
	@SuppressWarnings("unchecked")
	@Override
	public Status process() throws EventDeliveryException {
		if (accurateProcessStatus) {
			int count = this.connectionPoolSize - callTimeoutPool.getActiveCount();
			List<Callable<ProcessResult>> tasks = new ArrayList<Callable<ProcessResult>>();
			while (count > 0) {
				tasks.add(new Callable<ProcessResult>() {
					@Override
					public ProcessResult call() throws Exception {
						return doProcess();
					}
				});
				count--;
			}
			List<ListenableFuture<ProcessResult>> futures = null;
			try {
				futures = (List) listeningPool.invokeAll(tasks, SinkConsts.MIN_REQUEST_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				logger.warn(getName() + " fail to invoke process", e);
			}
			Status status = Status.READY;
			if (futures != null) {
				while (true) {
					boolean done = true;
					for (ListenableFuture<ProcessResult> future : futures) {
						if (!future.isDone() && !future.isCancelled()) {
							done = false;
							break;
						}
					}
					if (done) {
						break;
					}
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
					}
				}
				// 当出现一个backoff, 则返回backoff
				for (ListenableFuture<ProcessResult> future : futures) {
					try {
						if (!future.isCancelled() && future.get() == ProcessResult.FAIL) {
							status = status.BACKOFF;
							break;
						}
					} catch (Throwable e) {
						logger.warn(getName() + " fail to get process result throught callable future", e);
						status = status.BACKOFF;
						break;
					}
				}
			}

			// 为了实现failover, throw ex
			if (status == status.BACKOFF) {
				throw new EventDeliveryException("sink fail to send, throw ex for leading to failover.");
			} else {
				return status;
			}
		}
		else {
			if (callTimeoutPool.getActiveCount() < this.connectionPoolSize) {
				callTimeoutPool.submit(new Callable<ProcessResult>() {
					@Override
					public ProcessResult call() throws Exception {
						return doProcess();
					}
				});
			}
			else {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
				}
			}
			// logger.info(getName() + " processed 2 committed, accurateProcessStatus=" + accurateProcessStatus );
			return lastProcessResult == ProcessResult.READY ? Status.READY : Status.BACKOFF; // volatile varible 返回上次的状态，不准，且变化频繁
		}
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

		// public int currentPoolSize(){
		// return availableClients.size() + checkedOutClients.size();
		// }
		public AbstractMultiThreadRpcClient checkout() throws Exception {
			AbstractMultiThreadRpcClient ret = null;
			poolLock.lock();
			try {
				if (availableClients.isEmpty() && currentPoolSize < maxPoolSize) {
					ret = initializeRpcClient(clientProps);
					currentPoolSize++;
					checkedOutClients.add(ret);
					logger.info(String.format("%s add new rpc client. conn pool currentPoolSize=%d,maxPoolSize=%d", getName(),
							currentPoolSize,
							maxPoolSize));
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
				logger.info(String.format("%s removing rpc client. client.id=%s. currentPoolSize=%d", getName(), client.getName(),
						currentPoolSize));
				if (checkedOutClients.remove(client))
					currentPoolSize--;
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
					currentPoolSize--;
				}
				/*
				 * Be cruel and close even the checked out clients. The threads writing using these will now get an exception.
				 */
				for (AbstractMultiThreadRpcClient c : checkedOutClients) {
					c.close();
					currentPoolSize--;
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
