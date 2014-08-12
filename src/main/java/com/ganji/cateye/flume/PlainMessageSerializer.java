package com.ganji.cateye.flume;

import java.nio.ByteBuffer;

import scribe.thrift.LogEntry;

public class PlainMessageSerializer extends ScribeSerializer{

	@Override
	public ByteBuffer encodeToByteBuffer(LogEntry log, boolean compress) {
		return log.message;
	}

}
