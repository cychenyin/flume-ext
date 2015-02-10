package com.ganji.cateye.flume.kestrel.test;

import static org.junit.Assert.*;

import org.apache.thrift.TException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.ganji.cateye.flume.kestrel.Kestrel;

public class ClientUnitTest {
	Kestrel.Client client = null;
	
	public ClientUnitTest() {
	}

	@Before
	public void setUp() throws Exception {
//		 client = new KestrelThriftClient("sd-dw-rp-01.dns.ganji.com", 2229);
	}

	@After
	public void tearDown() throws Exception {
//		client.close();
	}

	@Test
	public void test() throws TException {
//		client.put("abpv+test+again", "yyyyyyyyyyyy", 3600000);
		
	}

}
