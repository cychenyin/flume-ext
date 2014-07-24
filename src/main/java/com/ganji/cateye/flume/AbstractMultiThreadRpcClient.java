package com.ganji.cateye.flume;

import java.util.List;
import java.util.Properties;

import org.apache.flume.Event;
import org.apache.flume.EventDeliveryException;
import org.apache.flume.FlumeException;
import org.apache.flume.api.AbstractRpcClient;
import org.apache.flume.api.RpcClient;
import org.apache.flume.api.RpcClientConfigurationConstants;

/*
 * 
 * used in set, force to imple hashcode & equals method
 */
public abstract class AbstractMultiThreadRpcClient extends AbstractRpcClient {

	@Override
	public abstract boolean equals(Object obj);
	
	@Override
	public abstract int hashCode();

	public abstract String getName();
	
}
