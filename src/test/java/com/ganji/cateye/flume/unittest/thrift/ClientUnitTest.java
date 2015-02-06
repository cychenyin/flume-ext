package com.ganji.cateye.flume.unittest.thrift;

import static org.junit.Assert.*;

import org.apache.thrift.TException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.ganji.cateye.flume.kestrel.KestrelThriftClient;

public class ClientUnitTest {
	KestrelThriftClient client;
	
	public ClientUnitTest() {
	}

	@Before
	public void setUp() throws Exception {
		client = new KestrelThriftClient("sd-dw-rp-01.dns.ganji.com", 2229);
	}

	@After
	public void tearDown() throws Exception {
		client.close();
	}

	@Test
	public void test() throws TException {
		client.put("abpv+test+again", "yyyyyyyyyyyy", 3600000);
		
	}

}
