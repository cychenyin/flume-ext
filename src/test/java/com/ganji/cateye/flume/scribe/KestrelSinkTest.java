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
import org.apache.flume.Event;
import org.apache.flume.channel.PseudoTxnMemoryChannel;
import org.apache.flume.event.SimpleEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.ganji.cateye.flume.kestrel.KestrelSink;
import com.ganji.cateye.flume.kestrel.KestrelSinkConstants;
import com.google.common.base.Charsets;

/**
 * Not really a unit test
 */
public class KestrelSinkTest {
	// private AsyncScribeSink sink = new AsyncScribeSink();
	private KestrelSink sink = new KestrelSink();

	@Test
	@Ignore
	public void testProcessA() throws Exception {
		process("cateye.a");
	}
	@Test
	@Ignore
	public void testProcessB() throws Exception {
		process("cateye.b");
	}
	@Test
	@Ignore
	public void testProcessC() throws Exception {
		process("cateye.c");
	}
	@Ignore
	@Test
	public void testProcessD() throws Exception {
		process("cateye.d");
		process("cateye.dd");
		process("cateye.ddd");
	}

	@Test
	public void testProcessE() throws Exception {
		process("cateye.e");
		process("cateye.x"); // 500 
		process("cateye.xx");
		
		process("cateye.y"); // 500
		process("cateye.year"); // 500
		// 991 + 1500 = 2491
	}

	private void process(String category) throws Exception {
		for (int i = 0; i < 500; i++) {
			Event e = new SimpleEvent();
			e.getHeaders().put("category", category);
			e.setBody("This is test ".getBytes(Charsets.UTF_8));
			sink.getChannel().put(e);
			sink.process();
		}
	}
	

	@Before
	public void setUp() throws Exception {
		Context ctx = new Context();
		ctx.put(KestrelSinkConstants.CONFIG_HOSTNAME, "10.7.5.31");
		ctx.put(KestrelSinkConstants.CONFIG_PORT, "2229");
		ctx.put(KestrelSinkConstants.CONFIG_SERIALIZER, "scribe");
		ctx.put(KestrelSinkConstants.CONFIG_BATCHSIZE, "10");

		ctx.put(KestrelSinkConstants.CONFIG_ROUTES, "q1 q2");
		ctx.put("route.q1.categories", "cateye.x cateye.y* cateye.a*");
		ctx.put("route.q1.queue", "flume1");

		ctx.put("route.q2.categories", "cateye.b cateye.c* cateye.dd*");
		ctx.put("route.q2.queue", "flume2");

		sink.configure(ctx);
		PseudoTxnMemoryChannel c = new PseudoTxnMemoryChannel();
		c.configure(ctx);
		c.start();
		sink.setChannel(c);
		sink.setName("kestrelTester");
		System.out.println(sink.getName());
		sink.start();
	}

	@After
	public void tearDown() throws Exception {
		Thread.sleep(10000);
		sink.getChannel().stop();
		sink.stop();
	}

}
