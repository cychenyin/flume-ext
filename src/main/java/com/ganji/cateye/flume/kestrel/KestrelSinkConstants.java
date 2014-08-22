package com.ganji.cateye.flume.kestrel;

import com.ganji.cateye.flume.SinkConstants;

public final class KestrelSinkConstants extends SinkConstants {
	// batchSize, request-timeout,
	public final static String CONFIG_HOSTNAME = "hostname";
	public final static String CONFIG_HOSTNAME_DEFAULT = "127.0.0.1";

	public final static String CONFIG_PORT = "port";
	public final static String CONFIG_PORT_DEFAULT = "2229";

	public static final String CONFIG_CATEGORY_HEADER = "scribe.category.header";
	public static final String CONFIG_CATEGORY_HEADER_DEFAULT = "category";

	public static final String CONFIG_COMPRESS = "compress";
	public static final String CONFIG_COMPRESS_DEFAULT = "False";

	public static final String CONFIG_SERIALIZER = "serializer";
	public static final String CONFIG_SERIALIZER_DEFAULT = "scribe";
	
	public static final String CONFIG_BATCHSIZE = "batchSize";
	public static final String CONFIG_BATCHSIZE_DEFAULT = "50";

	
	public static final String CONFIG_ROUTES = "routes";
	public static final String CONFIG_ROUTE_PREFIX = "route.";
	public static final String CONFIG_ROUTE_CATEGORY = ".categories";
	// kestrel queue name
	public static final String CONFIG_ROUTE_QUEUE = ".queue";
	
	@SuppressWarnings("unused")
	public final int RETRY_INTERVAL = 1 * 1000;

}
