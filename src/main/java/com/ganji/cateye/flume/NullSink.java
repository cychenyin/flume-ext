/*
 * create: asdf
 * create datetime: 2014-07-24 16:17:16 
 * 
 * */
package com.ganji.cateye.flume;

import org.apache.flume.Channel;
import org.apache.flume.ChannelException;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.EventDeliveryException;
import org.apache.flume.Transaction;
import org.apache.flume.api.RpcClientConfigurationConstants;
import org.apache.flume.conf.Configurable;
import org.apache.flume.instrumentation.SinkCounter;
import org.apache.flume.sink.AbstractSink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NullSink extends AbstractSink implements Configurable {
	private static final Logger LOGGER = LoggerFactory.getLogger(NullSink.class);
	private SinkCounter sinkCounter;
	int batchSize;
	
	@Override
	public void configure(Context context) {
		batchSize = Integer.parseInt(context.getString(RpcClientConfigurationConstants.CONFIG_BATCH_SIZE, "10000"));
		
		if (sinkCounter == null) {
			sinkCounter = new SinkCounter(getName());
		}
	}

	/**
	 * The start() of RpcSink is more of an optimization that allows connection to be created before the process() loop is started. In case
	 * it so happens that the start failed, the process() loop will itself attempt to reconnect as necessary. This is the expected behavior
	 * since it is possible that the downstream source becomes unavailable in the middle of the process loop and the sink will have to retry
	 * the connection again.
	 */
	@Override
	public void start() {
		sinkCounter.start();
		super.start();
	}

	@Override
	public void stop() {
		if (sinkCounter != null)
			sinkCounter.stop();

		super.stop();
	}

	@Override
	public String toString() {
		return "NullSink";
	}

	@Override
	public Status process() throws EventDeliveryException {
		Status status = Status.READY;
		Channel channel = getChannel();
		Transaction transaction = channel.getTransaction();
		try {
			transaction.begin();
			int size = 0;
			for (int i = 0; i < batchSize; i++) {
				Event event = channel.take();
				if (event == null) {
					break;
				}
				size ++;
			}

			
			if (size == 0) {
				sinkCounter.incrementBatchEmptyCount();
				status = Status.BACKOFF;
			} else {
				if (size < batchSize) {
					sinkCounter.incrementBatchUnderflowCount();
				} else {
					sinkCounter.incrementBatchCompleteCount();
				}
				sinkCounter.addToEventDrainAttemptCount(size);
			}
			transaction.commit();
			sinkCounter.addToEventDrainSuccessCount(size);
		} catch (Throwable t) {
			transaction.rollback();
			if (t instanceof Error) {
				throw (Error) t;
			} else if (t instanceof ChannelException) {
				LOGGER.error("Rpc Sink " + getName() + ": Unable to get event from" +
						" channel " + channel.getName() + ". Exception follows.", t);
				status = Status.BACKOFF;
			} else {
				throw new EventDeliveryException("Failed to send events", t);
			}
		} finally {
			transaction.close();
		}
		return status;
	}
}
