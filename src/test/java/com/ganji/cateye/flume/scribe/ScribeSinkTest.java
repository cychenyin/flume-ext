///*
// * Licensed to the Apache Software Foundation (ASF) under one
// * or more contributor license agreements.  See the NOTICE file
// * distributed with this work for additional information
// * regarding copyright ownership.  The ASF licenses this file
// * to you under the Apache License, Version 2.0 (the
// * "License"); you may not use this file except in compliance
// * with the License.  You may obtain a copy of the License at
// *
// * http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing,
// * software distributed under the License is distributed on an
// * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// * KIND, either express or implied.  See the License for the
// * specific language governing permissions and limitations
// * under the License.
// */
//package com.ganji.cateye.flume.scribe;
//
//import java.util.Date;
//
//import org.apache.flume.Context;
//import org.apache.flume.Event;
//import org.apache.flume.EventDeliveryException;
//import org.apache.flume.Sink.Status;
//import org.apache.flume.api.RpcClientConfigurationConstants;
//import org.apache.flume.channel.PseudoTxnMemoryChannel;
//import org.apache.flume.event.SimpleEvent;
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import com.google.common.base.Charsets;
//
///**
// * Not really a unit test
// */
//public class ScribeSinkTest {
//	private static final Logger LOGGER = LoggerFactory.getLogger(KestrelSinkTest.class);
//	private ScribeSink sink = new ScribeSink();
//	private boolean done = false;
//
//	@Test
//	public void testProcess() throws Exception {
//		for (int i = 0; i < 10; i++)
//		{
//			input(1000);
//			process();
//		}
//		done = true;
//	}
//
//	private void input(int count) {
//		for (int i = 1; i <= count; i++) {
//			Event e = new SimpleEvent();
//			if ((i % 1000) == 0 || i == count)
//				System.out.println("test data create & set " + i);
//
//			e.getHeaders().put(ScribeSinkConsts.DEFAULT_CATEGORY_HEADER_KEY, "test.d1");
//			e.setBody(("test " + i + "\n").getBytes(Charsets.UTF_8));
//			sink.getChannel().put(e);
//		}
//	}
//
//	private void process() throws EventDeliveryException, InterruptedException {
//		// Status status= sink.doProcess();
//
//		Status status = Status.READY; 
//		do  {
////			status = sink.doProcessRollback();
//			status = sink.process();
//			
//			Thread.sleep(10);
//		}while (status != Status.BACKOFF);
//		
//		System.out.println("process done successfully");
//	}
//
//	@Before
//	public void setUp() throws Exception {
//		Context ctx = new Context();
//		ctx.put(ScribeSinkConsts.CONFIG_SERIALIZER, ScribeSinkConsts.DEFAULT_SERIALIZER);
//		ctx.put(ScribeSinkConsts.CONFIG_HOSTNAME, "192.168.129.213");
//		ctx.put(ScribeSinkConsts.CONFIG_PORT, "31463");
//		// ctx.put(ScribeSinkConsts.CONFIG_HOSTNAME, "10.7.12.120");
//		// ctx.put(ScribeSinkConsts.CONFIG_PORT, "9080");
//
//		ctx.put(ScribeSinkConsts.CONFIG_CATEGORY_HEADER_KEY,
//				ScribeSinkConsts.DEFAULT_CATEGORY_HEADER_KEY);
//		ctx.put(ScribeSinkConsts.CONFIG_BATCHSIZE, "200");
//
//		ctx.put(RpcClientConfigurationConstants.CONFIG_CONNECTION_POOL_SIZE, "3");
//
//		sink.configure(ctx);
//		PseudoTxnMemoryChannel c = new PseudoTxnMemoryChannel(); 
//		// FileChannel c = new FileChannel();
//		Context cc = new Context();
//		cc.put("checkpointDir", "/data/checkpoint");
//		cc.put("dataDirs", "/data/data");
//		cc.put("capacity", "10000000");
//		cc.put("maxFileSize", "1024000");
//		cc.put("fsyncInterval", "1");
//		c.configure(cc);
//		c.setName("fchannel");
//		c.start();		
//		
//		sink.setChannel(c);
//		sink.setName("scribeSink");
//		sink.start();
//		
////		Transaction trans = c.getTransaction(); // 这行语句创建了transpaction, 否则c.put会异常退出(原因是c.put中不合理的状态检查代码)。
////		System.out.println(trans.getClass().getName());
//
//	}
//
//	@After
//	public void tearDown() throws Exception {
//		long start = (new Date()).getTime();
//		LOGGER.info("before teardown");
//		while (done == false) {
//			Thread.sleep(100);
//			long now = (new Date()).getTime();
//			if ((now - start) > 1 * 1000) {
//				LOGGER.info("tear down timeout");
//				break;
//			}
//		}
//		sink.getChannel().stop();
//		// Thread.sleep(1 * 1000);
//		sink.stop();
//		LOGGER.info("after teardown");
//	}
//}
