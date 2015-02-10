package com.ganji.cateye.flume.kestrel.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.EventDeliveryException;
import org.apache.flume.api.RpcClientConfigurationConstants;
import org.apache.flume.api.RpcClientFactory;
import org.apache.flume.event.SimpleEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.ganji.cateye.flume.kestrel.KestrelRpcClient;
import com.ganji.cateye.flume.kestrel.KestrelSinkConsts;
import com.google.common.base.Charsets;

/**
 * not real unit test exactly
 * @author asdf
 *
 */
public class KestrelRpcClientTest {
	KestrelRpcClient client;
	
	@Ignore
	@Test
	public void testSend() throws EventDeliveryException {
		input("cateye.aa", 800);
		
	}
	
	private void input(String category, int count) throws EventDeliveryException {
		List<Event> list = new ArrayList<Event>();
		
		for (int i = 1; i <= count; i++) {
			if((i % 50) == 0 || i == count)
				System.out.println("test data create & set " + i);
			
			Event e = new SimpleEvent();
			e.getHeaders().put("category", category);
			e.setBody(("This is test " + i).getBytes(Charsets.UTF_8));
			list.add(e);
			if(list.size() >= client.getBatchSize()) {
				client.appendBatch(list);
				list.clear();
			}
		}
		if(list.size() > 0) {
			client.appendBatch(list);
			list.clear();
		}
	}
	
	@Before
	public void setUp() throws Exception {
		

		Context ctx = new Context();
		ctx.put(KestrelSinkConsts.CONFIG_HOSTNAME, "10.7.5.31");
		ctx.put(KestrelSinkConsts.CONFIG_PORT, "2229");
		ctx.put(KestrelSinkConsts.CONFIG_SERIALIZER, "scribe");
		ctx.put(KestrelSinkConsts.CONFIG_BATCHSIZE, String.valueOf(100));

		ctx.put(KestrelSinkConsts.CONFIG_ROUTES, "q1 q2");
		ctx.put("route.q1.categories", "cateye.a* cateye.x cateye.y*");
		ctx.put("route.q1.queue", "flume5");

		ctx.put("route.q2.categories", "cateye.b cateye.c* cateye.dd*");
		ctx.put("route.q2.queue", "flume2");
		ctx.put("capacity", "100000");
		
		ctx.put(RpcClientConfigurationConstants.CONFIG_CONNECTION_POOL_SIZE, "1");
		
		Properties props = new Properties();
		for (Entry<String, String> entry : ctx.getParameters().entrySet()) {
			props.setProperty(entry.getKey(), entry.getValue());
		}
		props.setProperty("client.type", KestrelRpcClient.class.getCanonicalName());
		
		client = (KestrelRpcClient)RpcClientFactory.getInstance(props);
	}

	@After
	public void tearDown() throws Exception {
		client.close();
	}
}
