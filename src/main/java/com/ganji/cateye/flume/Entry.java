package com.ganji.cateye.flume;

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

		int count = args != null && args.length > 0 ? Integer.parseInt(args[0]) : Integer.MAX_VALUE;

		produce(count);
	}

	private static Stats stat = new Stats();

	private static void produce(int count) {
		DefaultMQProducer producer = new DefaultMQProducer("cateye");
		producer.setNamesrvAddr("192.168.129.213:9876");
		producer.setCreateTopicKey("cateye");
		producer.setProducerGroup("cateye");
		// producer.setInstanceName("cateye");

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
			int total = 5000; // 102400;
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
