/*jadclipse*/// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.

package com.ganji.cateye.flume.scribe;

import java.util.Properties;
import org.apache.flume.api.RpcClient;
import org.apache.flume.api.RpcClientFactory;
import org.apache.flume.sink.AbstractRpcSink;

/**
 * ScribeSink
 * agent.sinks.k1.type = com.ganji.cateye.flume.scribe2.ScribeSink
 * agent.sinks.k1.channel = c1 
 * agent.sinks.k1.hostname = 127.0.0.1
 * agent.sinks.k1.port = 31463
 * agent.sinks.k1.scribe.category.header=category
 * agent.sinks.k1.batch-size = 5555
 * // default batch-size=100
 * agent.sinks.k1.maxConnections=5
 *  // default maxConnections=5
 * agent.sinks.k1.request-timeout=3 * 1000
 *  // request-timeout=20s
 * @author asdf
 *
 */
public class ScribeSink extends AbstractRpcSink
{

    public ScribeSink()
    {
    }

    protected RpcClient initializeRpcClient(Properties props)
    {
        props.setProperty("client.type", com.ganji.cateye.flume.scribe.ScribeRpcClient.class.getCanonicalName());
        // props.setProperty("maxConnections", String.valueOf(1));
        return RpcClientFactory.getInstance(props);
    }
}
