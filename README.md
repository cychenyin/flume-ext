# flume-ext #

&#160; &#160; &#160; &#160;imple some flume plugins to support rocketmq sink , kestrel sink and wildcast channels  selector etc.
	any way, custorm plugins to meet ganji requirment.

----------------
## summary ##
----------------
	it's cateye's flume stuff modules. 


## Release history ##
----------------


- ### 1.0.1 
by chenyin
	add accurate_process_status configure option to AbstractMultiThreadRpcSink to support failover or lb
	change sinkConsts.MIN_REQUEST_TIMEOUT_MILLIS from 1000 to 5000

- ### 1.0.0
by chenyin
create AbstractMultiThreadRpcSink
create AbstractMultiThreadRpcClient
create MultiplexingChannelWildcardSelector
imple kestrelSink
imple rocketmqSink
imple scribeSink
custom imple scribeSource