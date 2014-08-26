package com.ganji.cateye.flume;

/**
 * {@code BasicConfigurationConstants }
 * {@code RpcClientConfigurationConstants }
 * @author asdf
 *
 */
public class SinkConsts {

    // 最小的请求超时时间
    public static final int MIN_REQUEST_TIMEOUT_MILLIS = 1000;

    // // connectionPoolSize * SinkRunner.maxBackoffSleep // Default= 5 * 5 = 25 second
    public static int THREADPOOL_AWAITTERMINATION_TIMEOUT = 4; 
}
