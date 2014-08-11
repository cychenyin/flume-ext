package com.ganji.cateye.flume.kestrel;

import java.io.IOException;
import java.util.List;

import scribe.thrift.LogEntry;

/**
 * 数据通道，实现通用的数据写入
 * @author tailor
 *
 */
public interface Channel {
	List<Category> getCategoryList();
	void prepare() throws Exception ;
	void write(List<LogEntry> messages, boolean endOfBatch);
}
