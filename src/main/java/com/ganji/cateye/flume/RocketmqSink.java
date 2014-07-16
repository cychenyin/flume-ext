package com.ganji.cateye.flume;

import java.util.Properties;

import org.apache.flume.api.RpcClient;
import org.apache.flume.api.RpcClientConfigurationConstants;
import org.apache.flume.api.RpcClientFactory;
import org.apache.flume.sink.AbstractRpcSink;
import org.apache.flume.sink.NullSink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * RocketMQ sink @see RocketmqRpcClient
 * configuration item supported:
 * hosts = h1 h2
 * hosts.h1 = 192.168.1.2:8080
 * hosts.h2 = 192.168.1.3:8080
 * hosts = 192.168.1.2:8080
 * compression-level = 6
 * batch-size = 1
 * request-timeout = 20
 * connect-timeout = 20
 * 	(both in second)
 * maxConnections = 5
 */
public class RocketmqSink extends AbstractRpcSink {
	private static final Logger logger = LoggerFactory.getLogger(RocketmqSink.class);

	@Override
	protected RpcClient initializeRpcClient(Properties props) {
		props.setProperty(RpcClientConfigurationConstants.CONFIG_CLIENT_TYPE,
				RocketmqRpcClient.class.getCanonicalName());
		// Only one thread is enough, since only one sink thread processes transactions at any given time. 
		// Each sink owns its own Rpc client.
		// props.setProperty(RpcClientConfigurationConstants.CONFIG_CONNECTION_POOL_SIZE, String.valueOf(1));

		return RpcClientFactory.getInstance(props);
	}

}
