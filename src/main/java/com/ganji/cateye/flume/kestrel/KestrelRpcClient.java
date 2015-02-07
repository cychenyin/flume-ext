package com.ganji.cateye.flume.kestrel;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.FlumeException;
import org.apache.flume.api.HostInfo;
import org.apache.flume.api.RpcClientConfigurationConstants;
import org.apache.flume.source.scribe.LogEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ganji.cateye.flume.AbstractMultiThreadRpcClient;
import com.ganji.cateye.flume.scribe.MessageSerializer;
import com.ganji.cateye.flume.scribe.PlainMessageSerializer;
import com.ganji.cateye.flume.scribe.ScribeSerializer;
//import com.ganji.cateye.flume.scribe.thrift.LogEntry;

/**
 * KestrelRpcClient
 * 
 * @author asdf
 * 
 */
public class KestrelRpcClient extends AbstractMultiThreadRpcClient {
	private static final Logger logger = LoggerFactory.getLogger(KestrelRpcClient.class);
	private String hostname;
	private int port;
	private String name = "";
	private String categoryHeaderKey;
	private final boolean compress = false;
	private MessageSerializer serializer;
	RouteConfig routes = new RouteConfig();
	private KestrelThriftClient client;
	
	public KestrelRpcClient() {
	}

	@SuppressWarnings("unused")
	private void dump(Properties properties) {
		for (Object key : properties.keySet()) {
			logger.warn("KestrelRpcClient dump conifg {}={}", key.toString(), properties.getProperty(key.toString()));
		}
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
				hostname = properties.getProperty(KestrelSinkConsts.CONFIG_HOSTNAME, KestrelSinkConsts.DEFAULT_HOSTNAME);
				port = Integer.parseInt(properties.getProperty(KestrelSinkConsts.CONFIG_PORT, KestrelSinkConsts.CONFIG_PORT_DEFAULT));
			}

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
			categoryHeaderKey = properties.getProperty(KestrelSinkConsts.CONFIG_CATEGORY_HEADER
					, KestrelSinkConsts.DEFAULT_CATEGORY_HEADER);

			Context context = new Context();
			context.put(KestrelSinkConsts.CONFIG_SERIALIZER, serializerName);
			context.put(KestrelSinkConsts.CONFIG_CATEGORY_HEADER, categoryHeaderKey);
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
			// sink global
			batchSize = Integer.parseInt(properties.getProperty(KestrelSinkConsts.CONFIG_BATCHSIZE,
					KestrelSinkConsts.DEFAULT_BATCHSIZE));
			requestTimeout = Long.parseLong(properties.getProperty(
					RpcClientConfigurationConstants.CONFIG_REQUEST_TIMEOUT,
					String.valueOf(RpcClientConfigurationConstants.DEFAULT_REQUEST_TIMEOUT_MILLIS)));
			if (requestTimeout < KestrelSinkConsts.MIN_REQUEST_TIMEOUT_MILLIS) {
				logger.warn("Request timeout specified less than 1s. Using default value instead.");
				requestTimeout = RpcClientConfigurationConstants.DEFAULT_REQUEST_TIMEOUT_MILLIS;
			}

			client = new KestrelThriftClient(hostname, port);
			name = String.format("%d@%s:%d", new Random().nextInt(), hostname, port);
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
			
			client.close();
			routes.clear();
			
			logger.info("KestrelRpcClient closed. name={}", name);
			
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
	
	@Override
	public void doAppendBatch(List<Event> events) throws Exception {
		// group log by route config
		Map<String, List<ByteBuffer>> items = new HashMap<String, List<ByteBuffer>>();
		for (Event event : events) {
			LogEntry log = serializer.serialize(event);
			String queue = routes.route(log.category);
			// avoid if queue is empty
			if (StringUtils.isNotEmpty(queue)) {
				List<ByteBuffer> list = items.get(queue);
				if (list == null) {
					list = new ArrayList<ByteBuffer>();
					items.put(queue, list);
				}
				list.add(serializer.encodeToByteBuffer(log, compress));
			}
		}
		// send
		for (Map.Entry<String, List<ByteBuffer>> e : items.entrySet()) {
			client.put(e.getKey(), e.getValue(), 0);
		}
		items.clear();
		items = null;
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

	// map kv = (cateogry, queueName)
	public static class RouteConfig {

		public final static String SPLITTER = "[\\s|,;]";
		// map kv = (cateogry, queueName); 其中category支持*通配符结尾， 且不移除通配符
		private Map<String, String> routes;
		private Set<String> wildcardCategories; // 其中不含*， 已被移除

		public RouteConfig() {
			routes = new HashMap<String, String>();
			wildcardCategories = new HashSet<String>();
		}

		// categories will be split
		public void add(String categories, String queue) {
			String[] ary = categories.split(RouteConfig.SPLITTER);
			for (String c : ary) {
				if (c.isEmpty())
					continue;
				if (c.endsWith("*")) {
					wildcardCategories.add(c.substring(0, c.length() - 1));
				}
				routes.put(c, queue);
			}
		}

		// find kestrel queue name thought category
		public String route(String category) {
			String ret = routes.get(category);
			if (StringUtils.isEmpty(ret)) {
				for (String w : wildcardCategories) {
					if (category.startsWith(w)) {
						ret = routes.get(w + "*");
						break;
					}
				}
			}
			return ret;
		}
		
		// clear
		public void clear() {
			this.routes.clear();
			this.wildcardCategories.clear();
		}
	}
}
