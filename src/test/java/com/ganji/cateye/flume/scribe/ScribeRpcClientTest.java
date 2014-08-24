package com.ganji.cateye.flume.scribe;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.EventDeliveryException;
import org.apache.flume.api.RpcClientConfigurationConstants;
import org.apache.flume.event.SimpleEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.base.Charsets;

/**
 * not real unit test exactly
 * @author asdf
 *
 */
public class ScribeRpcClientTest {
	ScribeRpcClient client;
	int total = 300000;
	 @Ignore
	@Test
	public void testSend() throws EventDeliveryException {
		total = 300000;
		input("cateye.aa", total);
	}
	
	private void input(String category, int count) throws EventDeliveryException {
		List<Event> list = new ArrayList<Event>();
		
		for (int i = 1; i <= count; i++) {
			if((i % 1000) == 0 || i == count)
				System.out.println("test data create & set " + i);
			
			Event e = new SimpleEvent();
			e.getHeaders().put("category", category);
			e.setBody(("This is test " + i + "\n").getBytes(Charsets.UTF_8));
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
	long start, end;
	@Before
	public void setUp() throws Exception {
		
		Context ctx = new Context();
		ctx.put(ScribeSinkConsts.CONFIG_HOSTNAME, "192.168.129.213");
		ctx.put(ScribeSinkConsts.CONFIG_PORT, "31463");
		ctx.put(ScribeSinkConsts.CONFIG_SERIALIZER, "scribe");
		ctx.put(ScribeSinkConsts.CONFIG_BATCHSIZE, String.valueOf(2000));
				
		ctx.put(RpcClientConfigurationConstants.CONFIG_CONNECTION_POOL_SIZE, "1");
		
		Properties props = new Properties();
		for (Entry<String, String> entry : ctx.getParameters().entrySet()) {
			props.setProperty(entry.getKey(), entry.getValue());
//			System.out.println(entry.getKey() + " = " + entry.getValue());
		}
//		System.out.println(props.getProperty("hostname"));
		client = new ScribeRpcClient();
		client.configure(props);
		start = (new Date()).getTime();
	}

	@After
	public void tearDown() throws Exception {
		end = (new Date()).getTime();
		System.out.println("cost = " + (end - start) + "ms");
		System.out.println("speed= " + total * 1000/(end - start) + "tps");
		client.close();
	}
}
