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
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import junit.framework.Assert;

import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.EventDeliveryException;
import org.apache.flume.Sink.Status;
import org.apache.flume.Transaction;
import org.apache.flume.api.RpcClientConfigurationConstants;
import org.apache.flume.channel.BasicTransactionSemantics;
import org.apache.flume.channel.MemoryChannel;
import org.apache.flume.channel.PseudoTxnMemoryChannel;
import org.apache.flume.event.SimpleEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ganji.cateye.flume.kestrel.KestrelRpcClient;
import com.ganji.cateye.flume.kestrel.KestrelSink;
import com.ganji.cateye.flume.kestrel.KestrelSinkConsts;
import com.google.common.base.Charsets;

/**
 * Not really a unit test
 */
public class KestrelSinkTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(KestrelSinkTest.class);
	// private AsyncScribeSink sink = new AsyncScribeSink();
	private KestrelSink sink = new KestrelSink();
	ThreadLocal<Var> currentVar = new ThreadLocal<KestrelSinkTest.Var>();

	public Var getVar() {
		Var var = currentVar.get();
		if (var == null) {
			var = new Var();
			currentVar.set(var);
		}
		return var;
	}

	@Ignore
	@Test
	public void testThreadLocalOneThread() {
		Var left = getVar();
		Var right = getVar();

		System.out.println(String.format("testThreadLocalOneThread left=%d, right=%d", left.hashCode(), right.hashCode()));

		Assert.assertTrue(left.hashCode() == right.hashCode());
		Assert.assertTrue(left.equals(right));
		Assert.assertTrue(left == right);

		done = true;
	}

	@Ignore
	@Test
	public void testThreadLocal() throws InterruptedException, ExecutionException {
		ExecutorService p = Executors.newFixedThreadPool(10);

		Future<Var> l = p.submit(new VarCallable(this));
		Future<Var> r = p.submit(new VarCallable(this));
		Var left = l.get();
		Var right = r.get();

		p.shutdown();
		p.awaitTermination(100, TimeUnit.MILLISECONDS);
		p.shutdownNow();

		System.out.println(String.format("testThreadLocal left=%d, right=%d", left.hashCode(), right.hashCode()));

		Assert.assertTrue(left.hashCode() != right.hashCode());
		Assert.assertTrue(!left.equals(right));
		Assert.assertTrue(left != right);

		done = true;
	}

	public static class VarCallable implements Callable<Var> {
		private KestrelSinkTest container;

		public VarCallable(KestrelSinkTest container) {
			this.container = container;
		}

		@Override
		public Var call() throws Exception {
			return container.getVar();
		}
	}

	public static class Var {
		private static Random r = new Random();

		public int hash = 0;

		public Var() {
			hash = r.nextInt();
		}

		@Override
		public int hashCode() {
			return hash;
		}

		@Override
		public String toString() {
			return String.valueOf(hash);
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Var) {
				return this.hash == ((Var) obj).hashCode();
			}
			return false;
		}
	}

	@Ignore
	@Test
	public void testTransaction5() throws InterruptedException, ExecutionException {
		Callable<Transaction> r = new Callable<Transaction>() {

			@Override
			public Transaction call() throws Exception {
				try {
					Thread.sleep((new Random()).nextInt(200));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				System.out.println("getting thread name=" + Thread.currentThread().getName() + " id=" + Thread.currentThread().getId()
						+ " now=" + (new Date()).getTime());
				Transaction t = sink.getChannel().getTransaction();
				t.begin();
				t.commit();
				t.close();
				System.out.println("closed thread name=" + Thread.currentThread().getName() + " id=" + Thread.currentThread().getId()
						+ " now=" + (new Date()).getTime());
				try {
					Thread.sleep((new Random()).nextInt(200));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				System.out.println("quitting thread name=" + Thread.currentThread().getName() + " id=" + Thread.currentThread().getId()
						+ " now=" + (new Date()).getTime());

				return t;
			}
		};
		ExecutorService p = Executors.newFixedThreadPool(10);

		Future<Transaction> f = p.submit(r);
		Future<Transaction> s = p.submit(r);
		Transaction tFirst = f.get();
		Transaction tSecond = s.get();

		System.out.println(String.format("testTransaction4 first=%d, second=%d", tFirst.hashCode(), tSecond.hashCode()));

		// tFirst.commit();
		// tFirst.close();
		// tSecond.commit();
		// tSecond.close();

		p.shutdown();
		p.awaitTermination(100, TimeUnit.MILLISECONDS);
		p.shutdownNow();

		// Assert.assertTrue(tFirst.hashCode() == tSecond.hashCode());
		// Assert.assertTrue(!tFirst.equals(tSecond));
		Assert.assertTrue(tFirst != tSecond);
		System.out.println("tFirst != tSecond =" + (tFirst != tSecond));

		System.out.println(String.format("testTransaction4 first=%d, second=%d", tFirst.hashCode(), tSecond.hashCode()));
		done = true;
	}

	@Ignore
	@Test
	public void testTransaction4() throws InterruptedException, ExecutionException {
		Callable<Integer> r = new Callable<Integer>() {
			@Override
			public Integer call() throws Exception {
				try {
					Thread.sleep((new Random()).nextInt(200));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				System.out.println("getting thread name=" + Thread.currentThread().getName() + " id=" + Thread.currentThread().getId()
						+ " now=" + (new Date()).getTime());
				Transaction t = sink.getChannel().getTransaction();
				t.begin();
				System.out.println("closed thread name=" + Thread.currentThread().getName() + " id=" + Thread.currentThread().getId()
						+ " now=" + (new Date()).getTime());

				// try {
				// Thread.sleep((new Random()).nextInt(200));
				// } catch (InterruptedException e) {
				// e.printStackTrace();
				// }
				t.commit();
				t.close();
				System.out.println("quitting thread name=" + Thread.currentThread().getName() + " id=" + Thread.currentThread().getId()
						+ " now=" + (new Date()).getTime());

				return t.hashCode();
			}
		};

		ExecutorService p = Executors.newFixedThreadPool(10);

		Future<Integer> f = p.submit(r);
		// Future<Integer> s = p.submit(r);
		Integer tFirst = f.get();
		// Integer tSecond = s.get();

		// System.out.println(String.format("testTransaction4 first=%d, second=%d", tFirst, tSecond));
		// Assert.assertTrue(tFirst == tSecond);
		// Assert.assertTrue(tFirst.equals(tSecond));
		// System.out.println(String.format("testTransaction4 first=%d, second=%d", tFirst.hashCode(), tSecond.hashCode()));

		p.shutdown();
		p.awaitTermination(100, TimeUnit.MILLISECONDS);
		p.shutdownNow();
		done = true;
	}

	@Ignore
	@Test
	public void testTransaction3() {
		Transaction tFirst = sink.getChannel().getTransaction();
		tFirst.begin();
		int first = tFirst.hashCode();
		tFirst.commit();
		tFirst.close();

		Transaction tSecond = sink.getChannel().getTransaction();
		tSecond.begin();
		int second = tSecond.hashCode();
		tSecond.commit();
		tSecond.close();

		Assert.assertTrue(first == second);
		Assert.assertTrue(tFirst == tSecond);
		Assert.assertTrue(tFirst.equals(tSecond));

		System.out.println(String.format("testTransaction3 first=%d, second=%d", first, second));
		done = true;
	}

	@Ignore
	@Test
	public void testTransaction2() {

		Transaction tFirst = sink.getChannel().getTransaction();
		tFirst.begin();
		int first = tFirst.hashCode();
		tFirst.close();

		Transaction tSecond = sink.getChannel().getTransaction();
		tSecond.begin();
		int second = tSecond.hashCode();
		tSecond.close();

		Assert.assertTrue(first == second);
		Assert.assertTrue(tFirst == tSecond);
		Assert.assertTrue(tFirst.equals(tSecond));

		System.out.println(String.format("testTransaction2 first=%d, second=%d", first, second));
		done = true;
	}

	@Ignore
	@Test
	public void testTransaction1() {
		Transaction tFirst = sink.getChannel().getTransaction();
		int first = tFirst.hashCode();

		tFirst.close();

		Transaction tSecond = sink.getChannel().getTransaction();
		int second = tSecond.hashCode();
		tSecond.close();

		Assert.assertTrue(first == second);
		Assert.assertTrue(tFirst == tSecond);
		Assert.assertTrue(tFirst.equals(tSecond));

		System.out.println(String.format("testTransaction1 first=%d, second=%d", first, second));
		done = true;
	}

	// 结论： 同一线程中获取到的transaction相同。
	@Ignore
	@Test
	public void testTransaction() {
		Transaction tFirst = sink.getChannel().getTransaction();
		int first = tFirst.hashCode();
		Transaction tSecond = sink.getChannel().getTransaction();
		int second = tSecond.hashCode();
		Assert.assertTrue(first == second);
		Assert.assertTrue(tFirst == tSecond);
		Assert.assertTrue(tFirst.equals(tSecond));
		System.out.println(String.format("testTransaction first=%d, second=%d", first, second));
		tFirst.close();
		tSecond.close();
		done = true;
	}

	// @Ignore
	@Test
	public void testProcessA() throws Exception {
		input("cateye.a", 1000);
		process();
	}

	private void process() throws EventDeliveryException, InterruptedException {
		
		Status status = sink.process();
		while (status != Status.BACKOFF) {
			status = sink.process();
			// Thread.sleep(1);h
		}
		// Status status= sink.doProcess();
		done = true;
		System.out.println("process done in tester");
	}

	@Test
	@Ignore
	public void testProcessB() throws Exception {
		input("cateye.b", 100);
		sink.process();
		done = true;
	}

	@Test
	@Ignore
	public void testProcessC() throws Exception {
		input("cateye.c", 100);
		sink.process();
		done = true;
	}

	@Ignore
	@Test
	public void testProcessD() throws Exception {
		input("cateye.d", 100);
		input("cateye.dd", 100);
		input("cateye.ddd", 100);
		sink.process();
		done = true;
	}

	@Test
	@Ignore
	public void testProcessE() throws Exception {
		input("cateye.e", 100);
		input("cateye.x", 100);
		input("cateye.xx", 100);

		input("cateye.y", 100);
		input("cateye.year", 100);
		sink.process();
		done = true;
	}

	@Ignore
	@Test
	public void testEquals() {
		Assert.assertTrue((new Integer(1)) != (new Integer(1)));
		Assert.assertTrue((new Integer(1)).equals(new Integer(1)));
		done = true;
	}

	private volatile boolean done = false;

	private void input(String category, int count) {
		for (int i = 1; i <= count; i++) {
			if((i % 50) == 0 || i == count)
				System.out.println("test data create & set " + i);
			Event e = new SimpleEvent();
			e.getHeaders().put("category", category);
			e.setBody(("This is test " + i).getBytes(Charsets.UTF_8));
			
			sink.getChannel().put(e);
		}
	}

	@Before
	public void setUp() throws Exception {
		done = false;

		Context ctx = new Context();
		ctx.put(KestrelSinkConsts.CONFIG_HOSTNAME, "10.7.5.31");
		ctx.put(KestrelSinkConsts.CONFIG_PORT, "2229");
		ctx.put(KestrelSinkConsts.CONFIG_SERIALIZER, "scribe");
		ctx.put("batch-size", String.valueOf(100));
		
		ctx.put(KestrelSinkConsts.CONFIG_ROUTES, "q1 q2");
		ctx.put("route.q1.categories", "cateye.x cateye.y* cateye.a*");
		ctx.put("route.q1.queue", "flume1");

		ctx.put("route.q2.categories", "cateye.b cateye.c* cateye.dd*");
		ctx.put("route.q2.queue", "flume2");
		ctx.put("capacity", "100000");
		
		ctx.put(RpcClientConfigurationConstants.CONFIG_CONNECTION_POOL_SIZE, "3");
		sink.configure(ctx);
		
        PseudoTxnMemoryChannel c = new PseudoTxnMemoryChannel();
        c.configure(ctx);
        c.start();
        sink.setChannel(c);
        sink.setName("kestrelSinkTester");
        sink.start();
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
		sink.getChannel().stop();
		// Thread.sleep(1 * 1000);
		sink.stop();
		LOGGER.info("after teardown");
	}
}
