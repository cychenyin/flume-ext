package com.ganji.cateye.flume;

import java.nio.ByteBuffer;

import org.apache.flume.Event;
import org.apache.flume.conf.Configurable;

//import com.ganji.cateye.flume.scribe.thrift.LogEntry;
import org.apache.flume.source.scribe.LogEntry;


public interface MessageSerializer extends Configurable {
	// byte[] serialize(Event event);
	LogEntry serialize(Event event);
	 // ByteBuffer encodeToByteBuffer(byte[], boolean compress); 
	ByteBuffer encodeToByteBuffer(LogEntry log, boolean compress);
}
