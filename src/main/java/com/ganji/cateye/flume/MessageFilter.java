package com.ganji.cateye.flume;

import com.ganji.cateye.flume.scribe.thrift.LogEntry;

// import scribe.thrift.LogEntry;

public interface MessageFilter {
	public LogEntry process(LogEntry entry);
}
