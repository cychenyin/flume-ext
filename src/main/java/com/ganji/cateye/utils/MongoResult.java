package com.ganji.cateye.utils;

import com.mongodb.WriteResult;

public class MongoResult {
	
	public MongoResult() {}
	public MongoResult(WriteResult result, long elapseTime) {
		this.result = result;
		this.elapsedTime = elapseTime;
	}
	private long elapsedTime;
	private WriteResult result;

	public WriteResult getResult() {
		return result;
	}

	public void setResult(WriteResult result) {
		this.result = result;
	}

	public long getElapsedTime() {
		return elapsedTime;
	}

	public void setElapsedTime(long elapsedTime) {
		this.elapsedTime = elapsedTime;
	}

}
