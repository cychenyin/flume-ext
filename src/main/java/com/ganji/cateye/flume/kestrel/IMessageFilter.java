package com.ganji.cateye.flume.kestrel;

import scribe.thrift.LogEntry;

public interface IMessageFilter {
	public LogEntry process(LogEntry entry);
}
