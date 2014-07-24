package com.ganji.cateye.flume;

import java.util.List;
import java.util.Properties;

import org.apache.flume.Event;
import org.apache.flume.EventDeliveryException;
import org.apache.flume.FlumeException;
import org.apache.flume.api.RpcClient;
import org.apache.flume.api.RpcClientConfigurationConstants;

public abstract class AbstractMultiThreadRpcClient implements RpcClient {

	protected int batchSize = RpcClientConfigurationConstants.DEFAULT_BATCH_SIZE;
	protected long connectTimeout = RpcClientConfigurationConstants.DEFAULT_CONNECT_TIMEOUT_MILLIS;
	protected long requestTimeout = RpcClientConfigurationConstants.DEFAULT_REQUEST_TIMEOUT_MILLIS;
	
	@Override
	public int getBatchSize() {
		return batchSize;
	}

	public abstract void start() throws EventDeliveryException;
	@Override
	public abstract void append(Event event) throws EventDeliveryException;

	@Override
	public abstract void appendBatch(List<Event> events)
			throws EventDeliveryException;

	@Override
	public abstract boolean isActive();

	@Override
	public abstract void close() throws FlumeException;

	/**
	 * Configure the client using the given properties object.
	 * 
	 * @param properties
	 * @throws FlumeException
	 *             if the client can not be configured using this method, or if the client was already configured once.
	 */
	protected abstract void configure(Properties properties)
			throws FlumeException;

	@Override
	public abstract boolean equals(Object obj);
	
	@Override
	public abstract int hashCode();
	
}
