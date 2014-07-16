package com.ganji.cateye.utils;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 
 * @author asdf
 */
public class AutoExecutor {

	public AutoExecutor() {
		this(30);
	}
	public AutoExecutor(int seconds) {
		this.seconds = seconds;
	}
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	
	private int seconds;
	public int getSeconds() {
		return seconds;
	}
	
	public void setSeconds(int seconds) {
		this.seconds = seconds;
	}
	public void shutdown() {
		this.scheduler.shutdown();
	}
	public void executeOnce(final IAutoExcecutor runner) {
		scheduler.schedule(new Runnable() {
			@Override
			public void run() {
				if(runner != null)
					runner.excecute();
			}
		}, this.seconds, TimeUnit.SECONDS);
	}
	public void executeInBackground(final IAutoExcecutor runner) {
		scheduler.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				if(runner != null)
					runner.excecute();
			}
		}, 0, seconds, TimeUnit.SECONDS);
	}
}
