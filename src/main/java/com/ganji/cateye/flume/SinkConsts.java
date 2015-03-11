package com.ganji.cateye.flume;

/**
 * {@code BasicConfigurationConstants }
 * {@code RpcClientConfigurationConstants }
 * @author asdf
 *
 */
public class SinkConsts {

    // 最小的请求超时时间
    public static final int MIN_REQUEST_TIMEOUT_MILLIS = 30000;
    public static final String CONFIG_REQUEST_TIMEOUT_MILLIS = "request_timeout_millis";
    
    // // connectionPoolSize * SinkRunner.maxBackoffSleep // Default= 5 * 5 = 25 second
    public static int THREADPOOL_AWAITTERMINATION_TIMEOUT = 4; 
    
    // 在sink中是否精确的返回process的调用结果；否的话返回上次的结果，然后直接退出
    public static final String CONFIG_ACCURATE_PROCESS_STATUS = "accurate_process_status";
    public static final boolean DEFAULT_ACCURATE_PROCESS_STATUS = true;

	public static final String CONFIG_CATEGORY_HEADER = "scribe.category.header";
	public static final String DEFAULT_CATEGORY_HEADER = "category";
}
