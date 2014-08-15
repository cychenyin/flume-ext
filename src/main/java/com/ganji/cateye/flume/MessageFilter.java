package com.ganji.cateye.flume;

import com.ganji.cateye.flume.scribe.thrift.LogEntry;

public interface MessageFilter {
	public LogEntry process(LogEntry entry);
}
