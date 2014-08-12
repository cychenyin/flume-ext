package com.ganji.cateye.flume;

import scribe.thrift.LogEntry;

public interface MessageFilter {
	public LogEntry process(LogEntry entry);
}
