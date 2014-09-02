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

import java.util.Date;

import org.apache.flume.Channel;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.EventDeliveryException;
import org.apache.flume.Sink.Status;
import org.apache.flume.api.RpcClientConfigurationConstants;
import org.apache.flume.channel.PseudoTxnMemoryChannel;
import org.apache.flume.event.SimpleEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ganji.cateye.flume.AbstractMultiThreadRpcSink;
import com.google.common.base.Charsets;

/**
 * Not really a unit test
 */
public abstract class SinkTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(KestrelSinkTest.class);
	protected boolean done = false;

	protected abstract AbstractMultiThreadRpcSink getSink();
	@Test
	public void testProcess() throws Exception {
		for (int i = 0; i < 10; i++)
		{
			input(1000);
			process();
		}
		Thread.sleep(1000);
		process();
		System.out.println("all done;");
		done = true;
	}

	protected void input(int count) {
		Channel channel = getSink().getChannel();
		for (int i = 1; i <= count; i++) {
			Event e = new SimpleEvent();
			if ((i % 1000) == 0 || i == count)
				System.out.println("test data create & set " + i);

			e.getHeaders().put(ScribeSinkConsts.DEFAULT_CATEGORY_HEADER_KEY, "test.d1");
			e.setBody(("test " + i + "\n").getBytes(Charsets.UTF_8));
			channel.put(e);
		}
	}

	protected void process() throws EventDeliveryException, InterruptedException {
		Status status = Status.READY; 
		do  {
//			status = sink.doProcessRollback();
			status = getSink().process();
			Thread.sleep(10);
		}while (status != Status.BACKOFF);
		
		System.out.println("process done successfully");
	}


	@After
	public void tearDown() throws Exception {
		long start = (new Date()).getTime();
		LOGGER.info("before teardown");
		while (done == false) {
			Thread.sleep(100);
			long now = (new Date()).getTime();
			if ((now - start) > 1 * 1000) {
				LOGGER.info("tear down timeout");
				break;
			}
		}
		getSink().getChannel().stop();
		// Thread.sleep(1 * 1000);
		getSink().stop();
		LOGGER.info("after teardown");
	}
}
