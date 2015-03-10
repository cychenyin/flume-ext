/*jadclipse*/// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.

package com.ganji.cateye.flume.kestrel;

import java.util.Properties;

import org.apache.flume.api.RpcClient;
import org.apache.flume.api.RpcClientFactory;
import org.apache.flume.sink.AbstractRpcSink;

import com.ganji.cateye.flume.scribe.ScribeSinkConsts;

/**
 * KestrelSink
 * 	send scribe logEntry to kestrel
 * 	configuration example:
 * agent.sinks.k1.type = com.ganji.cateye.flume.kestrel.KestrelSink
 * agent.sinks.k1.channel = c1 
 * agent.sinks.k1.hostname = 127.0.0.1
 * agent.sinks.k1.port = 2229
 * agent.sinks.k1.maxConnections = 1
 * agent.sinks.k1.batch-size = 9999
 * agent.sinks.k1.request-timeout = 5000 
 * agent.sinks.k1.hostname = localhost
 * agent.sinks.k1.port = 2229
 * agent.sinks.k1.serializer = plain-message
 * agent.sinks.k1.scribe.category.header = category
 * agent.sinks.k1.routes = pv wap rta test
 * agent.sinks.k1.route.pv.categories = ms.pv
 * agent.sinks.k1.route.pv.queue = rta-pvlog-1
 * agent.sinks.k1.route.wap.categories = mobile.wap.pv
 * agent.sinks.k1.route.wap.queue = rta-waplog
 * agent.sinks.k1.route.rta.categories = rta.mobile.client.*
 * agent.sinks.k1.route.rta.queue = rta-wap-pvlog-1
 * agent.sinks.k1.route.test.categories = test.*
 * agent.sinks.k1.route.test.queue = ftest  
 * @author asdf
 * 2015-02-10
 */
public class KestrelSink extends AbstractRpcSink
{

    public KestrelSink()
    {
    }

    protected RpcClient initializeRpcClient(Properties props)
    {
        props.setProperty("client.type", com.ganji.cateye.flume.kestrel.KestrelRpcClient.class.getCanonicalName());
        // props.setProperty("maxConnections", String.valueOf(1));
        if(!props.containsKey(ScribeSinkConsts.CONFIG_SINK_NAME)) {
        	props.setProperty(ScribeSinkConsts.CONFIG_SINK_NAME, this.getName());
        }
        return RpcClientFactory.getInstance(props);
    }
}
