/*jadclipse*/// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.

package com.ganji.cateye.flume.scribe2;

import java.util.Properties;
import org.apache.flume.api.RpcClient;
import org.apache.flume.api.RpcClientFactory;
import org.apache.flume.sink.AbstractRpcSink;

// Referenced classes of package org.apache.flume.sink:
//            AbstractRpcSink

public class ScribeSink extends AbstractRpcSink
{

    public ScribeSink()
    {
    }

    protected RpcClient initializeRpcClient(Properties props)
    {
        props.setProperty("client.type", com.ganji.cateye.flume.scribe2.ScribeRpcClient.class.getCanonicalName());
        // props.setProperty("maxConnections", String.valueOf(1));
        return RpcClientFactory.getInstance(props);
    }
}
