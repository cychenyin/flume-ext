package com.ganji.cateye.flume.kestrel;

public class KestrelSinkConstants {
	// batchSize, request-timeout,
	public final static String CONFIG_HOSTNAME = "hostname";
	public final static String CONFIG_HOSTNAME_DEFAULT = "127.0.0.1";

	public final static String CONFIG_PORT = "port";
	public final static String CONFIG_PORT_DEFAULT = "2229";

	public static final String CONFIG_CATEGORY_HEADER = "scribe.header";
	public static final String CONFIG_CATEGORY_HEADER_DEFAULT = "category";

	public static final String CONFIG_FORCECATEGORY = "forceCategory";

	public static final String CONFIG_COMPRESS = "compress";
	public static final String CONFIG_COMPRESS_DEFAULT = "False";

	public static final String CONFIG_SERIALIZERID = "serializerId";
	public static final String CONFIG_SERIALIZERID_DEFAULT = "scribe";

}
