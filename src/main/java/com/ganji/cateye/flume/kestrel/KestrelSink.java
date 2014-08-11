/*
 */

package com.ganji.cateye.flume.kestrel;

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

/*// TODO 修改下参数注释
 * Kestrel sink @see KestrelRpcClient
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
public class KestrelSink extends AbstractMultiThreadRpcSink {
	private static final Logger logger = LoggerFactory.getLogger(KestrelSink.class);

	@Override
	protected AbstractMultiThreadRpcClient initializeRpcClient(Properties props) {
		if (!props.containsKey(RpcClientConfigurationConstants.CONFIG_CLIENT_TYPE)) {
			props.setProperty(RpcClientConfigurationConstants.CONFIG_CLIENT_TYPE,
					KestrelRpcClient.class.getCanonicalName());
		}
		// set or override setting here.
		
		AbstractMultiThreadRpcClient ret = null;
		try {
		ret = (AbstractMultiThreadRpcClient) RpcClientFactory.getInstance(props);
		
		}catch(Throwable e){
			System.out.println(e.getMessage());
		}
		
		
		return ret;
	}
}
