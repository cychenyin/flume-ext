package com.ganji.cateye.flume.kestrel;

import com.ganji.cateye.flume.SinkConsts;

public final class KestrelSinkConsts extends SinkConsts {
	// batchSize, request-timeout,
	public final static String CONFIG_HOSTNAME = "hostname";
	public final static String DEFAULT_HOSTNAME = "127.0.0.1";

	public final static String CONFIG_PORT = "port";
	public final static String CONFIG_PORT_DEFAULT = "2229";

	public static final String CONFIG_CATEGORY_HEADER = "scribe.category.header";
	public static final String DEFAULT_CATEGORY_HEADER = "category";

	// 是否压缩
	public static final String CONFIG_COMPRESS = "compress";
	public static final String DEFAULT_COMPRESS = "False";

	public static final String CONFIG_SERIALIZER = "serializer";
	public static final String DEFAULT_SERIALIZER = "scribe";
	
	public static final String CONFIG_BATCHSIZE = "batchSize";
	public static final String DEFAULT_BATCHSIZE = "50";

	// 配置category到kestrel queue的路由
	public static final String CONFIG_ROUTES = "routes";
	public static final String CONFIG_ROUTE_PREFIX = "route.";
	public static final String CONFIG_ROUTE_CATEGORY = ".categories";
	// kestrel queue name config suffix
	public static final String CONFIG_ROUTE_QUEUE = ".queue";
	
	@SuppressWarnings("unused")
	public final int RETRY_INTERVAL = 1 * 1000;

}
