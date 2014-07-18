package com.ganji.cateye.flume;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.rocketmq.client.exception.MQBrokerException;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.client.producer.DefaultMQProducer;
import com.alibaba.rocketmq.common.message.Message;
import com.alibaba.rocketmq.remoting.exception.RemotingException;

public class Entry {
	private static final Logger logger = LoggerFactory.getLogger(Entry.class.getName());

	public static void main(String[] args) {
		if (logger.isWarnEnabled()) {
			logger.warn("server starting");
		}

		int count = args != null && args.length > 0 ? Integer.parseInt(args[0]) : 10;
		sendlogBackground(count);
		System.out.println("done.");

//		System.out.println(Entry.class.getCanonicalName());
		// a(10);
//		System.out.println("done 2. ");
	}

	private static void sink() {

	}

	private static void produce(int count) {
		DefaultMQProducer producer = new DefaultMQProducer("cateye");
		producer.setNamesrvAddr("192.168.129.213:9876");
		// producer.setProducerGroup("cateye");
		// producer.setInstanceName("CateyeProducer");
		try {
			producer.start();
		} catch (MQClientException e) {
			e.printStackTrace();
		}
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<5000; i++){
			sb.append("1");
		};
		String body = sb.toString();
		try {
			for (int i = 0; i < 1000; i++) {
				Message m = new Message("cateye", "ms.web.app", body.getBytes());
				producer.send(m);
				Thread.sleep(10);
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

	// private static void sendlogForkjoin(int logCount) {
	// // ForkJoinPool pool = new ForkJoinPool();
	// }

	private static void sendlogBackground(int logCount) {
		if (logCount < 1000) {
			sendlog(logCount);
			return;
		}
		int threadCount = 10;
		final int batchSize = logCount / threadCount;
		List<Thread> threads = new ArrayList<Thread>(threadCount);
		for (int i = 0; i < threadCount; i++) {
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					sendlog(batchSize);
				}
			});
			threads.add(t);
			t.start();
		}

		for (int i = 0; i < threadCount; i++) {
			try {
				threads.get(i).join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private static String body_;
	
	private static String body() {
		if(body_ == null) {
			int total = 102400;
			StringBuilder sb = new StringBuilder();
			sb.append("size=");
			sb.append(total);
			Random rnd = new Random();
			while(sb.length() < total) {
				sb.append(rnd.nextInt());
			}
			body_ = sb.toString();
		}
		return body_;
	}
	private static void sendlog(int count) {
		int rnd = new Random().nextInt();

		for (int i = 1; i <= count; i++) {
			if (i % 100 == 0) {
				System.out.println("flume test log, rnd=" + rnd + " count=" + i);
			}
			logger.warn("flume test log, rnd={} count={} {}", rnd, i, body());
		}
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
