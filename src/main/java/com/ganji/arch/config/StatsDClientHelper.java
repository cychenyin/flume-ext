package com.ganji.arch.config;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.StatsDClient;
import com.timgroup.statsd.StatsDClientException;

public class StatsDClientHelper {
	private final Logger logger = LoggerFactory
			.getLogger(StatsDClientHelper.class);
	private StatsDClient client;
	private StatsDClient sqlClient;

	private String host;
	private int port;
	private SystemConfig config ;
	public StatsDClientHelper() {
		config = SystemConfig.getInstance();
		host = config.Statsd_Host;
		port = Integer.parseInt(config.Statsd_Port);
		this.init();
	}

	// @PostConstruct
	private void init() {
		try {
			client = new NonBlockingStatsDClient(
					config.Statsd_Prefix, host, port);
			sqlClient = new NonBlockingStatsDClient(
					config.Slow_Sql_Statsd_Prefix, host,
					port);
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
		sqlClient.stop();
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

	/**
	 * 日志计数; 将level, key组合 后写入prefix前缀的aspect 例如：prefix=cateye level=info
	 * key=ms.js 则写入的日志有： cateye.info.- cateye.info.ms.- cateye.info.ms.js.-
	 * 创建的目录有：cateye.info; cateye.info.ms; cateye.info.js
	 * 
	 * @param prefix
	 *            {@link String}; statsd Aspect前缀， 创建StatsdClient实例使用
	 * @param level
	 *            {@link String}; 日志级别，可以为fatal, warn, error, info, debug
	 * @param key
	 *            {@link String}; 可以为空，如果为空,则对level进行计数
	 */
	public void incrementCounter(String prefix, String level, String key) {
		if (StringUtils.isBlank(prefix)|| StringUtils.isBlank(level)) {
			if (logger.isErrorEnabled()) {
				logger.error(String.format(
						"incrementCounter error, prefix=%s, level=%s ", prefix,
						level));
			}
			return;
		}
		StatsDClient statsd = prefix == config.Slow_Sql_Statsd_Prefix ? this.sqlClient
				: this.client;
		// level计数
		statsd.incrementCounter(level + ".-");
		// 分拆key计数
		if (!StringUtils.isBlank(key)) {
			String[] keys = key.split("\\.");
			StringBuilder builder = new StringBuilder();
			builder.append(level).append(".");
			for (String akey : keys) {
				builder.append(akey);
				// client.increment(builder.toString() + "_");
				statsd.increment(builder.toString() + ".-");
				builder.append(".");
			}
		}
	}
}
