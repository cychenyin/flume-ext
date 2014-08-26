/*
 */

package com.ganji.cateye.flume.scribe;

import java.util.Properties;

import org.apache.flume.api.RpcClient;
import org.apache.flume.api.RpcClientConfigurationConstants;
import org.apache.flume.api.RpcClientFactory;
import org.apache.flume.sink.AbstractRpcSink;
import org.apache.flume.sink.NullSink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ganji.cateye.flume.AbstractMultiThreadRpcClient;
import com.ganji.cateye.flume.AbstractMultiThreadRpcSink;

/**
 * Scribe sink @see ScribeRpcClient 示例： agent.sinks.statSink.type = com.ganji.cateye.flume.kestrel.KestrelSink agent.sinks.statSink.channel
 * = c2 agent.sinks.statSink.batchSize = 50 agent.sinks.statSink.hostname = 10.7.5.31 agent.sinks.statSink.port = 2229
 * agent.sinks.statSink.serializer = scribe agent.sinks.statSink.scribe.category.header = category
 */
public class ScribeSink extends AbstractMultiThreadRpcSink {
	private static final Logger logger = LoggerFactory.getLogger(ScribeSink.class);

	@Override
	protected AbstractMultiThreadRpcClient initializeRpcClient(Properties props) {
		if (!props.containsKey(RpcClientConfigurationConstants.CONFIG_CLIENT_TYPE)) {
			props.setProperty(RpcClientConfigurationConstants.CONFIG_CLIENT_TYPE,
					ScribeRpcClient.class.getCanonicalName());
		}
		// set or override setting here.
		AbstractMultiThreadRpcClient ret = null;
		ret = (AbstractMultiThreadRpcClient) RpcClientFactory.getInstance(props);
//		try {
//		} catch (Throwable e) {
//			logger.error("fail to create kestrel rpc client.", e);
//		}
		return ret;
	}
}
