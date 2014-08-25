package com.ganji.cateye.flume;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.apache.flume.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.rocketmq.client.exception.MQBrokerException;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.client.producer.DefaultMQProducer;
import com.alibaba.rocketmq.common.message.Message;
import com.alibaba.rocketmq.remoting.exception.RemotingException;
import com.ganji.cateye.flume.rocketmq.RocketmqSink;
import com.ganji.cateye.flume.scribe.ScribeSinkConsts;
import com.ganji.cateye.flume.scribe.simple.EventToLogEntrySerializer;
import com.ganji.cateye.utils.Stats;

public class Entry {
	private static final Logger logger = LoggerFactory.getLogger(Entry.class.getName());

	public static void main(String[] args) {
		System.out.println("entry point");
		
		long convert = TimeUnit.MILLISECONDS.convert(20, TimeUnit.SECONDS);
		System.out.println(convert);
		// int threadCount = args != null && args.length > 0 ? Integer.parseInt(args[0]) : 4;
		// int count = args != null && args.length > 1 ? Integer.parseInt(args[1]) : Integer.MAX_VALUE;
		// produce(threadCount, count);
		// System.out.println("done.");
	}

	@SuppressWarnings("unused")
	private static void sink() {
		RocketmqSink sink = new RocketmqSink();

		Properties props = new Properties();
		props.setProperty("host.h1", "192.168.129.213:9876");
		props.setProperty("hostname", "192.168.129.213");
		props.setProperty("port", "9876");
		// AbstractMultiThreadRpcClient c = sink.initializeRpcClient(props);
		// c.close();
		sink.configure(createContext());
		sink.stop();
		
	}

	private static Context createContext() {
		Context ctx = new Context();
        ctx.put(ScribeSinkConsts.CONFIG_SERIALIZER, EventToLogEntrySerializer.class.getName());
        ctx.put(ScribeSinkConsts.CONFIG_HOSTNAME, "127.0.0.1");
        ctx.put(ScribeSinkConsts.CONFIG_PORT, "1463");
        ctx.put(ScribeSinkConsts.CONFIG_CATEGORY_HEADER_KEY, ScribeSinkConsts.DEFAULT_CATEGORY_HEADER_KEY);
        return ctx;
	}
	private static Stats stat = new Stats();

	@SuppressWarnings("unused")
	private static void produce(int threadCount, int count) {
		threadCount = threadCount < 1 ? 1 : threadCount;
		final int batchSize = count < Integer.MAX_VALUE ? count / threadCount : Integer.MAX_VALUE;
		List<Thread> threads = new ArrayList<Thread>(threadCount);
		for (int i = 0; i < threadCount; i++) {
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					produce(batchSize);
				}
			});
			threads.add(t);
			t.start();
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
			}
		}
		for (int i = 0; i < threadCount; i++) {
			try {
				threads.get(i).join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private static void produce(int count) {
		DefaultMQProducer producer = new DefaultMQProducer("cateye");
		producer.setNamesrvAddr("192.168.129.213:9876");
		producer.setCreateTopicKey("cateye");
		producer.setProducerGroup("cateye");
		producer.setInstanceName("cateye" + (new Random()).nextInt());

		try {
			producer.start();
		} catch (MQClientException e) {
			e.printStackTrace();
		}

		try {
			for (int i = 1; i <= count; i++) {
				Message m = new Message("cateye", "flume.bench", body());
				producer.send(m);
				stat.increase();

				if (i % 1000 == 0) {
					System.out.println(stat.toString());
				}
			}

		} catch (MQClientException e) {
			e.printStackTrace();
		} catch (RemotingException e) {
			e.printStackTrace();
		} catch (MQBrokerException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			producer.shutdown();
		}
	}

	private static byte[] body_;

	private static byte[] body() {
		if (body_ == null) {
			int total = 2000; // 102400;
			StringBuilder sb = new StringBuilder();
			sb.append("size=");
			sb.append(total);
			Random rnd = new Random();
			while (sb.length() < total) {
				sb.append(rnd.nextInt());
			}
			body_ = sb.toString().getBytes();
		}
		return body_;
	}

	public static void usage() {
		StringBuilder sb = new StringBuilder();
		sb.append("SightServiceServer {option}");
		sb.append("\n");
		sb.append("	option: -s -simple -m -multithread -n -nonblocking");
		sb.append("\n");
		sb.append("		-simple, use TSimpleServer");
		sb.append("\n");
		sb.append("		-s same to -simple");
		sb.append("\n");
		sb.append("		-poolthread, use TThreadPoolServer");
		sb.append("\n");
		sb.append("		-p same to -poolthread");
		sb.append("\n");
		sb.append("		-nonblocking, use TNonblockingServer");
		sb.append("\n");
		sb.append("		-n same to -nonblocking");
		sb.append("\n");
		sb.append("		-threadedSelector, use TThreadedSelectorServer");
		sb.append("\n");
		sb.append("		-t same to -threadedSelector");
		sb.append("\n");
		System.out.println(sb.toString());
	}

}
