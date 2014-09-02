package com.ganji.cateye.flume;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.flume.Event;
import org.apache.flume.EventDeliveryException;
import org.apache.flume.api.AbstractRpcClient;

/*
 * AbstractMultiThreadRpcClient, force sub class to imple hashcode & equals method
 * ATTIONS : 
 * 	1. set connState in configure method
 *  2. use stateLock when state changing  
 *  3. 
 */
public abstract class AbstractMultiThreadRpcClient extends AbstractRpcClient {

	protected final Lock stateLock;
	protected State connState;

	public AbstractMultiThreadRpcClient() {
		stateLock = new ReentrantLock(true);
		connState = State.INIT;
	}

	@Override
	public boolean isActive() {
		stateLock.lock();
		try {
			return (connState == State.READY);
		} finally {
			stateLock.unlock();
		}
	}

	@Override
	public abstract boolean equals(Object obj);

	@Override
	public abstract int hashCode();

	public abstract String getName();

	public abstract void doAppendBatch(final List<Event> events) throws Exception;

	@Override
	public void append(final Event event) throws EventDeliveryException {
		List<Event> events = new ArrayList<Event>();
		events.add(event);
		appendBatch(events);
	}

	@Override
	public void appendBatch(final List<Event> events) throws EventDeliveryException {
		try {
			if (!isActive()) {
				throw new EventDeliveryException("Client was closed due to error.  Please create a new client");
			}
			doAppendBatch(events);
			
		} catch (Throwable e) {
			if (e instanceof ExecutionException) {
				Throwable cause = e.getCause();
				if (cause instanceof TimeoutException) {
					throw new EventDeliveryException("Send call timeout", cause);
				} else if (cause instanceof InterruptedException) {
					throw new EventDeliveryException("Send call cause of interrupted exception. ", cause);
				}
			}
			if (e instanceof Error) {
				throw (Error) e;
			} else if (e instanceof RuntimeException) {
				throw (RuntimeException) e;
			}
			throw new EventDeliveryException("Failed to send event. ", e);
		}

	}
	protected static enum State {
		INIT, READY, DEAD
	}
}
