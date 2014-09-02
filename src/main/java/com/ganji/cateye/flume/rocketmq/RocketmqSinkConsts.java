package com.ganji.cateye.flume.rocketmq;

import org.apache.flume.api.RpcClientConfigurationConstants;

public final class RocketmqSinkConsts {
	// batchSize, request-timeout,
	public final static String CONFIG_HOSTNAME = "hostname";
	public final static String DEFAULT_HOSTNAME = "127.0.0.1";

	// roecketmq name server's port
	public final static String CONFIG_PORT = "port";
	public final static String DEFAULT_PORT = "9876";

	public static final String CONFIG_CATEGORY_HEADER_KEY = "scribe.header";
	public static final String DEFAULT_CATEGORY_HEADER_KEY = "category";

	public static final String CONFIG_FORCECATEGORY = "forceCategory";

	public static final String CONFIG_COMPRESS = "compress";
	public static final String DEFAULT_COMPRESS = "False";

	public static final String CONFIG_SERIALIZER = "serializer";
	public static final String DEFAULT_SERIALIZER = "scribe";

	public static final String CONFIG_TOPIC = "topic";
	public static final String DEFAULT_TOPIC = "cateye";

	public static final String CONFIG_PRODUCERGROUP = "producerGroup";
	public static final String DEFAULT_PRODUCERGROUP = "cateye";

	public static final String CONFIG_COMPRESSMSGBODYOVERHOWMUCH = "compressMsgBodyOverHowmuch";
	public static final String DEFAULT_COMPRESSMSGBODYOVERHOWMUCH = "4000";

	public static final String CONFIG_BATCHSIZE = "batchSize";
	public static final String DEFAULT_BATCH_SIZE = "50";
}
