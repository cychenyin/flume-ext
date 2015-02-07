package com.ganji.cateye.flume.scribe;

// import com.ganji.cateye.flume.scribe.thrift.LogEntry;
import org.apache.flume.source.scribe.LogEntry;

public interface MessageFilter {
	public LogEntry process(LogEntry entry);
}
