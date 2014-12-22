package com.ganji.cateye.flume.scribe;

import java.util.Properties;

import org.apache.flume.api.RpcClient;
import org.apache.flume.api.RpcClientFactory;
import org.apache.flume.sink.AbstractRpcSink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleScribeSink extends AbstractRpcSink
{
	private static final Logger logger = LoggerFactory.getLogger(SimpleScribeSink.class);
    public SimpleScribeSink()
    {
    }

    protected RpcClient initializeRpcClient(Properties props)
    {
        logger.info("Attempting to create SimpleScribeSink Rpc client.");
        return RpcClientFactory.getInstance(props);
    }


}