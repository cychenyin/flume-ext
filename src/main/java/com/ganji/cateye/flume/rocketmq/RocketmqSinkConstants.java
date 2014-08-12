package com.ganji.cateye.flume.rocketmq;

import org.apache.flume.api.RpcClientConfigurationConstants;

public final class RocketmqSinkConstants {
	// batchSize, request-timeout,
	public final static String CONFIG_HOSTNAME = "hostname";
	public final static String CONFIG_HOSTNAME_DEFAULT = "127.0.0.1";

	// roecketmq name server's port
	public final static String CONFIG_PORT = "port";
	public final static String CONFIG_PORT_DEFAULT = "9876";

	public static final String CONFIG_CATEGORY_HEADER = "scribe.header";
	public static final String CONFIG_CATEGORY_HEADER_DEFAULT = "category";

	public static final String CONFIG_FORCECATEGORY = "forceCategory";

	public static final String CONFIG_COMPRESS = "compress";
	public static final String CONFIG_COMPRESS_DEFAULT = "False";

	public static final String CONFIG_SERIALIZERID = "serializerId";
	public static final String CONFIG_SERIALIZERID_DEFAULT = "scribe";

	public static final String CONFIG_TOPIC = "topic";
	public static final String CONFIG_TOPIC_DEFAULT = "cateye";

	public static final String CONFIG_PRODUCERGROUP = "producerGroup";
	public static final String CONFIG_PRODUCERGROUP_DEFAULT = "cateye";

	public static final String CONFIG_COMPRESSMSGBODYOVERHOWMUCH = "compressMsgBodyOverHowmuch";
	public static final String CONFIG_COMPRESSMSGBODYOVERHOWMUCH_DEFAULT = "4000";

	public static final String DEFAULT_BATCH_SIZE = "50";
}
