package com.ganji.cateye.flume;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Entry {
	private static final Logger logger = LoggerFactory.getLogger(Entry.class);
	public static class A {
		public static void a() {
			logger.error("abc aaaaaaaaaaaaa");		
		}	
		
		public static void B() {
			logger.error("abc aaaaaaaaaaaaa");		
			a();
		}	
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		logger.error("test error");
		logger.warn("test warn");
		logger.info("test info");
		logger.trace("test trace");
		A.B();
	}

	
	
}
