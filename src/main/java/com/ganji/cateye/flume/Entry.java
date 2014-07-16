package com.ganji.cateye.flume;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Entry {
	private static final Logger logger = LoggerFactory.getLogger(Entry.class.getName());

	public static void main(String[] args) {
		if (logger.isWarnEnabled()) {
			logger.warn("server starting");
		}

		int count = args != null && args.length > 0 ? Integer.parseInt(args[0]) : 1;
		// sendlog(count);
		// sendlogBackground(count);
		System.out.println("done.");
		
		System.out.println(Entry.class.getCanonicalName());
	}

	// private static void sendlogForkjoin(int logCount) {
	// // ForkJoinPool pool = new ForkJoinPool();
	// }

	private static void sendlogBackground(int logCount) {
		if (logCount < 1000) {
			sendlog(logCount);
			return;
		}
		int threadCount = 4;
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

	private static void sendlog(int count) {
		int rnd = new Random().nextInt();
		int midLen = 5000;
		StringBuilder sb = new StringBuilder();
		sb.append(" size=5k ");
		for (int i = 0; i < midLen; i++) {
			sb.append("-");
		}
		
		for (int i = 1; i <= count; i++) {
			if (i % 1000 == 0) {
				System.out.println("flume test log, rnd=" + rnd + "count=" + i);
			}
			logger.warn("flume test log, rnd={} count={} {}", rnd, i, sb.toString());
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
