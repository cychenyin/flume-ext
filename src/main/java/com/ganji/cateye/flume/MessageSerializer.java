package com.ganji.cateye.flume;

import java.nio.ByteBuffer;

import org.apache.flume.Event;
import org.apache.flume.conf.Configurable;

import com.ganji.cateye.flume.scribe.thrift.LogEntry;


public interface MessageSerializer extends Configurable {
	LogEntry serialize(Event event);
	
	ByteBuffer encodeToByteBuffer(LogEntry log, boolean compress); 
}
