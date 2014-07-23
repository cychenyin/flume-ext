package com.ganji.cateye.utils;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ganji.arch.config.SystemConfig;
import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.StatsDClient;
import com.timgroup.statsd.StatsDClientException;

public class StatsDClientHelper {
	private final Logger logger = LoggerFactory
			.getLogger(StatsDClientHelper.class);
	private StatsDClient client;

	private String host;
	private int port;

	public StatsDClientHelper() {
		host = SystemConfig.getInstance().Statsd_Host;
		port = Integer.parseInt(SystemConfig.getInstance().Statsd_Port);
		this.init();
	}

	private void init() {

		try {
			client = new NonBlockingStatsDClient(SystemConfig.getInstance().Statsd_Prefix, host, port);
		} catch (StatsDClientException e) {
			e.printStackTrace();
			if (logger.isErrorEnabled()) {
				logger.error(e.getMessage(), e);
			}
			throw e;
		}
	}

	public void stop() {
		client.stop();
	}

	@Override
	public void finalize() throws Throwable {
		this.stop();
		super.finalize();
	}

	public static String format(String key) {
		String[] keys = key.split("\\.");
		String result = key;
		try {
			result = String.format("%s._%s", key, keys[keys.length - 1]);
		} catch (Exception e) {
		}

		return result;
	}

	public void incrementCounter(String key, int count) {
		if (StringUtils.isBlank(key)) {
			if (logger.isErrorEnabled()) {
				logger.error(String.format("incrementCounter error, key=%s", key));
			}
			return;
		}
		// level计数
		this.client.count(key, count);
	}
}
