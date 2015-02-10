/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
/*
 * create: asdf
 * create datetime: 2015-02-06 16:17:16 
 * 
 * */

package com.ganji.cateye.flume.kestrel;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang.StringUtils;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.EventDeliveryException;
import org.apache.flume.FlumeException;
import org.apache.flume.api.AbstractRpcClient;
import org.apache.flume.api.HostInfo;
import org.apache.flume.api.RpcClientConfigurationConstants;
import org.apache.flume.source.scribe.LogEntry;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ganji.cateye.flume.MessageSerializer;
import com.ganji.cateye.flume.PlainMessageSerializer;
import com.ganji.cateye.flume.ScribeSerializer;
import com.ganji.cateye.flume.SinkConsts;

public class KestrelRpcClient extends AbstractRpcClient {
	private static final Logger logger = LoggerFactory.getLogger(KestrelRpcClient.class);

	private int batchSize;
	private long requestTimeout;
	private final Lock stateLock;
	private State connState;
	private String hostname;
	private int port;
	private ConnectionPoolManager connectionManager;
	private final ExecutorService callTimeoutPool;
	private final AtomicLong threadCounter;
	private int connectionPoolSize;
	private final Random random = new Random();
	private MessageSerializer serializer;
	RouteConfig routes = new RouteConfig();

	public KestrelRpcClient() {
		stateLock = new ReentrantLock(true);
		connState = State.INIT;

		threadCounter = new AtomicLong(0);
		// OK to use cached threadpool, because this is simply meant to timeout
		// the calls - and is IO bound.
		callTimeoutPool = Executors.newCachedThreadPool(new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				t.setName("ScribeRpcClientThread-" + String.valueOf(threadCounter.incrementAndGet()));
				return t;
			}
		});
	}

	@Override
	public int getBatchSize() {
		return batchSize;
	}

	@Override
	public void append(Event event) throws EventDeliveryException {
		// Thrift IPC client is not thread safe, so don't allow state changes or
		// client.append* calls unless the lock is acquired.
		ClientWrapper client = null;
		boolean destroyedClient = false;
		try {
			if (!isActive()) {
				throw new EventDeliveryException("Client was closed due to error. " +
						"Please create a new client");
			}
			client = connectionManager.checkout();
			// final ThriftFlumeEvent thriftEvent = new ThriftFlumeEvent(event
			// .getHeaders(), ByteBuffer.wrap(event.getBody()));

			doAppend(client, event).get(requestTimeout, TimeUnit.MILLISECONDS);
		} catch (Throwable e) {
			if (e instanceof ExecutionException) {
				Throwable cause = e.getCause();
				if (cause instanceof EventDeliveryException) {
					throw (EventDeliveryException) cause;
				} else if (cause instanceof TimeoutException) {
					throw new EventDeliveryException("Append call timeout", cause);
				}
			}
			destroyedClient = true;
			// If destroy throws, we still don't want to reuse the client, so mark it
			// as destroyed before we actually do.
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
		// Thrift IPC client is not thread safe, so don't allow state changes or client.append* calls unless the lock is acquired.
		ClientWrapper client = null;
		boolean destroyedClient = false;
		try {
			if (!isActive()) {
				throw new EventDeliveryException("Client was closed due to error or is not yet configured.");
			}
			client = connectionManager.checkout();
			doAppendBatch(client, events).get(requestTimeout, TimeUnit.MILLISECONDS);			
		} catch (Throwable e) {
			if (e instanceof ExecutionException) {
				Throwable cause = e.getCause();
				if (cause instanceof EventDeliveryException) {
					throw (EventDeliveryException) cause;
				} else if (cause instanceof TimeoutException) {
					throw new EventDeliveryException("Append call timeout", cause);
				}
			}
			destroyedClient = true;
			// If destroy throws, we still don't want to reuse the client, so mark it
			// as destroyed before we actually do.
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

	private Future<Void> doAppend(final ClientWrapper client, final Event e) throws Exception {

		return callTimeoutPool.submit(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				LogEntry log = serializer.serialize(e);
				String queue = routes.route(log.category);
				List<ByteBuffer> list = new ArrayList<ByteBuffer>();
				list.add(serializer.encodeToByteBuffer(log, false));
				int result = client.client.put(queue, list, 0);
				if (result != 1) {
					logger.warn(" kestrel client send return " + String.valueOf(result) + " [should be 1]");
					throw new EventDeliveryException("Failed to deliver events. Server returned status : " + String.valueOf(result) + " [should be 1]");
				} else if(logger.isInfoEnabled()) {
					logger.info(String.format("kestrel client success send event 1"));
				}
				return null;
			}
		});
	}

	private Future<Void> doAppendBatch(final ClientWrapper client, final List<Event> e) throws Exception {

		return callTimeoutPool.submit(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				Map<String, List<ByteBuffer>> items = new HashMap<String, List<ByteBuffer>>();
				for (Event event : e) {
					LogEntry log = serializer.serialize(event);
					String queue = routes.route(log.category);
					// avoid if queue is empty
					if (StringUtils.isNotEmpty(queue)) {
						List<ByteBuffer> list = items.get(queue);
						if (list == null) {
							list = new ArrayList<ByteBuffer>();
							items.put(queue, list);
						}
						list.add(serializer.encodeToByteBuffer(log, false));
					}
				}
				// send
				int result = 0;
				for (Map.Entry<String, List<ByteBuffer>> e : items.entrySet()) {
					result += client.client.put(e.getKey(), e.getValue(), 0);
				}

				if (result != e.size()) {
					String msg = String.format(" kestrel client %d send return %d [should be %d]", client.hashCode(), result, e.size());
					logger.warn(msg);
					throw new EventDeliveryException("Failed to deliver events. " + msg);
				} else if(logger.isInfoEnabled()) {
					logger.info(String.format("kestrel client %d success send events %d", client.hashCode(), e.size()));
				}

				return null;
			}
		});
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

	@Override
	protected void configure(Properties properties) throws FlumeException {
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
				hostname = properties.getProperty(KestrelSinkConsts.CONFIG_HOSTNAME, KestrelSinkConsts.DEFAULT_HOSTNAME);
				port = Integer.parseInt(properties.getProperty(KestrelSinkConsts.CONFIG_PORT, KestrelSinkConsts.CONFIG_PORT_DEFAULT));
			}
			batchSize = Integer.parseInt(properties.getProperty(
					RpcClientConfigurationConstants.CONFIG_BATCH_SIZE,
					RpcClientConfigurationConstants.DEFAULT_BATCH_SIZE.toString()));
			requestTimeout = Long.parseLong(properties.getProperty(
					RpcClientConfigurationConstants.CONFIG_REQUEST_TIMEOUT,
					String.valueOf(
							RpcClientConfigurationConstants.DEFAULT_REQUEST_TIMEOUT_MILLIS)));
			if (requestTimeout < 1000) {
				logger.warn("Request timeout specified less than 1s. Using default value instead.");
				requestTimeout = RpcClientConfigurationConstants.DEFAULT_REQUEST_TIMEOUT_MILLIS;
			}
			connectionPoolSize = Integer.parseInt(properties.getProperty(
					RpcClientConfigurationConstants.CONFIG_CONNECTION_POOL_SIZE,
					String.valueOf(RpcClientConfigurationConstants.DEFAULT_CONNECTION_POOL_SIZE)));
			if (connectionPoolSize < 1) {
				logger.warn("Connection Pool Size specified is less than 1. Using default value instead.");
				connectionPoolSize = RpcClientConfigurationConstants.DEFAULT_CONNECTION_POOL_SIZE;
			}
			connectionManager = new ConnectionPoolManager(connectionPoolSize);

			// serialization
			String serializerName = properties.getProperty(KestrelSinkConsts.CONFIG_SERIALIZER, KestrelSinkConsts.DEFAULT_SERIALIZER);
			if (serializerName.equalsIgnoreCase(KestrelSinkConsts.DEFAULT_SERIALIZER)) {
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
			Context context = new Context();
			context.put(SinkConsts.CONFIG_CATEGORY_HEADER,
					properties.containsKey(SinkConsts.CONFIG_CATEGORY_HEADER) ? properties.getProperty(SinkConsts.CONFIG_CATEGORY_HEADER)
							: SinkConsts.DEFAULT_CATEGORY_HEADER);
			serializer.configure(context);
			// routes
			String rs = properties.getProperty(KestrelSinkConsts.CONFIG_ROUTES, "");
			if (StringUtils.isEmpty(rs))
				throw new FlumeException("routes of KestrelRpcClient not configed");
			String[] arrRoute = rs.split(RouteConfig.SPLITTER);
			for (String route : arrRoute) {
				if (route.isEmpty())
					continue;
				String prefix = KestrelSinkConsts.CONFIG_ROUTE_PREFIX + route;
				routes.add(properties.getProperty(prefix + KestrelSinkConsts.CONFIG_ROUTE_CATEGORY),
						properties.getProperty(prefix + KestrelSinkConsts.CONFIG_ROUTE_QUEUE));
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

	private static enum State {
		INIT, READY, DEAD
	}

	/**
	 * Wrapper around a client and transport, so we can clean up when this client gets closed.
	 */
	private class ClientWrapper {
		public final Kestrel.Client client;
		// public final TFastFramedTransport transport;
		public final TFramedTransport transport;
		private final int hashCode;

		public ClientWrapper() throws Exception {
			transport = new TFramedTransport(new TSocket(hostname, port));
			transport.open();
			// client = new Kestrel.Client(new TCompactProtocol(transport));
			client = new Kestrel.Client(new TBinaryProtocol(transport));
			
			// Not a great hash code, but since this class is immutable and there
			// is at most one instance of the components of this class,
			// this works fine [If the objects are equal, hash code is the same]
			hashCode = random.nextInt();
		}

		public boolean equals(Object o) {
			if (o == null) {
				return false;
			}
			// Since there is only one wrapper with any given client,
			// direct comparison is good enough.
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
			} finally {
				poolLock.unlock();
			}
			client.transport.close();
		}

		public void closeAll() {
			poolLock.lock();
			try {
				for (ClientWrapper c : availableClients) {
					c.transport.close();
					currentPoolSize--;
				}
				/*
				 * Be cruel and close even the checked out clients. The threads writing using these will now get an exception.
				 */
				for (ClientWrapper c : checkedOutClients) {
					c.transport.close();
					currentPoolSize--;
				}
			} finally {
				poolLock.unlock();
			}
		}
	}
}
