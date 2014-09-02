/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.ganji.cateye.flume.scribe;

import org.apache.flume.Context;
import org.apache.flume.api.RpcClientConfigurationConstants;
import org.apache.flume.channel.PseudoTxnMemoryChannel;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ganji.cateye.flume.AbstractMultiThreadRpcSink;
import com.ganji.cateye.flume.rocketmq.RocketmqSink;
import com.ganji.cateye.flume.rocketmq.RocketmqSinkConsts;

/**
 * Not really a unit test
 */
public class RocketmqSinkTest extends SinkTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(RocketmqSinkTest.class);
	private RocketmqSink sink;	

	@Before
	public void setUp() throws Exception {
		sink = new RocketmqSink();
		Context ctx = new Context();
		ctx.put(RocketmqSinkConsts.CONFIG_SERIALIZER, RocketmqSinkConsts.DEFAULT_SERIALIZER);
		ctx.put(RocketmqSinkConsts.CONFIG_HOSTNAME, "192.168.129.213");
		ctx.put(RocketmqSinkConsts.CONFIG_PORT, "9876");

		ctx.put(RocketmqSinkConsts.CONFIG_CATEGORY_HEADER_KEY, RocketmqSinkConsts.DEFAULT_CATEGORY_HEADER_KEY);
		ctx.put(RocketmqSinkConsts.CONFIG_BATCHSIZE, "200");
		ctx.put(RpcClientConfigurationConstants.CONFIG_CONNECTION_POOL_SIZE, "3");
		
		sink.setName("rocketmqSink");
		sink.configure(ctx);
		PseudoTxnMemoryChannel c = new PseudoTxnMemoryChannel();
		// FileChannel c = new FileChannel();
		Context cc = new Context();
		cc.put("capacity", "10000000");
		// cc.put("checkpointDir", "/data/checkpoint");
		// cc.put("dataDirs", "/data/data");
		// cc.put("maxFileSize", "1024000");
		// cc.put("fsyncInterval", "1");
		c.configure(cc);
		c.setName("chnl");
		c.start();

		sink.setChannel(c);
		sink.start();
	}
	
	@Override
	protected AbstractMultiThreadRpcSink getSink() {
		return sink;
	}
}
