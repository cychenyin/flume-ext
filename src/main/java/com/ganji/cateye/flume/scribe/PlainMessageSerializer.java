package com.ganji.cateye.flume.scribe;

import java.nio.ByteBuffer;

import org.apache.commons.codec.Charsets;

//import com.ganji.cateye.flume.scribe.thrift.LogEntry;
import org.apache.flume.source.scribe.LogEntry;

public class PlainMessageSerializer extends ScribeSerializer{

	@Override
	public ByteBuffer encodeToByteBuffer(LogEntry log, boolean compress) {
		// code for 
		// return log.message;
		
		// slower then thrift 0.8+ version cause of mem copy
		return ByteBuffer.wrap(log.message.getBytes(Charsets.UTF_8));
	}

}
