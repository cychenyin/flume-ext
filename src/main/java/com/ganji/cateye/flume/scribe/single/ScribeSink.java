/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.ganji.cateye.flume.scribe.single;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.apache.flume.Channel;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.EventDeliveryException;
import org.apache.flume.Transaction;
import org.apache.flume.conf.Configurable;
import org.apache.flume.instrumentation.SinkCounter;
import org.apache.flume.sink.AbstractSink;
import org.apache.flume.source.scribe.LogEntry;
import org.apache.flume.source.scribe.ResultCode;
import org.apache.flume.source.scribe.Scribe;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ganji.cateye.flume.ScribeSerializer;
import com.ganji.cateye.flume.SinkConsts;
import com.ganji.cateye.flume.scribe.ScribeSinkConsts;
//import com.ganji.cateye.flume.scribe.thrift.*;

/**
 * Synchronous Sink that forwards messages to a scribe listener.
 * <p>
 * The use case for this sink is to maintain backward compatibility with scribe consumers when there is a desire to migrate from Scribe
 * middleware to FlumeNG.
 * agent
 * agent.sinks.k1.type = com.ganji.cateye.flume.scribe.single.ScribeSink
 * agent.sinks.k1.channel = c1 
 * agent.sinks.k1.hostname = 127.0.0.1
 * agent.sinks.k1.port = 31463
 * agent.sinks.k1.batch-size = 200
 * agent.sinks.k1.scribe.category.header=category
 */
public class ScribeSink extends AbstractSink implements Configurable {
	private static final Logger logger = LoggerFactory.getLogger(ScribeSink.class);
	private long batchSize = 1;
	private SinkCounter sinkCounter;
	private ScribeSerializer serializer;
	private Scribe.Client client;
	private TTransport transport;

	@Override
	public void configure(Context context) {
		sinkCounter = new SinkCounter(getName());
		batchSize = context.getLong(ScribeSinkConsts.CONFIG_BATCHSIZE, 1L);
		serializer = new ScribeSerializer();
		serializer.configure(context);

		
		String host = context.getString(ScribeSinkConsts.CONFIG_HOSTNAME);
		int port = context.getInteger(ScribeSinkConsts.CONFIG_PORT);

		try {
			logger.warn("single.ScribeSink.host={} port={}", host, port);
			transport = new TFramedTransport(new TSocket(new Socket(host, port)));
			logger.warn("scribeSink has created transport");
		} catch (Exception ex) {
			logger.error("Unable to create Thrift Transport, host=" + host + ":port=" + port, ex);
			throw new RuntimeException(ex);
		}
	}

	@Override
	public synchronized void start() {
		super.start();
		client = new Scribe.Client(new TBinaryProtocol(transport, false, false));
		sinkCounter.start();
	}

	@Override
	public synchronized void stop() {
		super.stop();
		sinkCounter.stop();
		transport.close();
		client = null;
	}

	@Override
	public Status process() throws EventDeliveryException {
		logger.debug("start processing");

		Status status = Status.READY;
		Channel channel = getChannel();
		List<LogEntry> eventList = new ArrayList<LogEntry>();
		Transaction transaction = channel.getTransaction();
		transaction.begin();

		for (int i = 0; i < batchSize; i++) {
			Event event = channel.take();
			if (event == null) {
				status = Status.BACKOFF;
				sinkCounter.incrementBatchUnderflowCount();
				break;
			}
			else {
				eventList.add(serializer.serialize(event));
			}
		}

		sendEvents(transaction, eventList);
		return status;
	}

	private void sendEvents(Transaction transaction, List<LogEntry> eventList)
			throws EventDeliveryException {
		try {
			sinkCounter.addToEventDrainAttemptCount(eventList.size());
			ResultCode rc = eventList.size() > 0 ? client.Log(eventList) : ResultCode.OK;
			if (rc.equals(ResultCode.OK)) {
				transaction.commit();
				sinkCounter.addToEventDrainSuccessCount(eventList.size());
				if(logger.isInfoEnabled()) {
					logger.info(String.format("%s success send events %d", this.getName(), eventList.size()));
				}
			}
			transaction.commit();
			sinkCounter.addToEventDrainSuccessCount(eventList.size());
		} catch (Throwable e) {
			transaction.rollback();
			logger.error(this.getName() + " exception while processing in Scribe Sink", e);
			throw new EventDeliveryException("Failed to send message", e);
		} finally {
			transaction.close();
		}
	}
}
