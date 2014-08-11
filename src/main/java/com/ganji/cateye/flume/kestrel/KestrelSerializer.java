package com.ganji.cateye.flume.kestrel;

import java.nio.ByteBuffer;

import org.apache.flume.Event;
import org.apache.flume.conf.Configurable;

import scribe.thrift.LogEntry;

public interface KestrelSerializer extends Configurable {
	LogEntry serialize(Event event);
	
	ByteBuffer encodeToByteBuffer(LogEntry log, boolean compress); 
}
