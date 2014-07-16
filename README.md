sight
==========================
----------------
summary：
----------------
&nbsp;&nbsp;&nbsp;&nbsp;目前我们每天产生的Scribe的日志有7000万，开发人员会被日志的红海淹没，所以需要有一个途径能够更直观的了解到底有哪些不同的日志，到底每种日志一段时间有多少个，有没有心的日志出来。这个项目的目标就是解决上述的问题。
该项目与cateye/aquila项目结合使用，但是不局限于日志的处理。请根据自己的业务场景确定是否可以使用该项目。
    
>>see also:
>1. mmseg
>2. text analyzer 
>3. token analyzer 
>4. lru
>5. simhash


concepts：
----------------
doc, 文档的主体是文本；需要对文档有唯一标识; 系统处理结果中的文本是经过过滤的
samilar, 对于段文本敏感，对位置变动不敏感，对大量重复敏感
	
features:
----------------	
1. 对频繁出现的文本进行聚类
2. data storage support file system & mongodb 
3. backend

issues && todo：
----------------    
1. 随机用户数据识别不够好
 

service.thrift interface：
----------------
```thrift
service.thrift

# Interface definition for sight service
#

namespace java com.ganji.cateye.thrift.sight
namespace php cateye
namespace py cateye

# *** PLEASE REMEMBER TO EDIT THE VERSION CONSTANT WHEN MAKING CHANGES ***
const string VERSION = "1.0.0"
const i32 SUCCESS = 200;
const i32 ERROR = 500;

#
# data structures
#

struct InputMsg {
	1: required string id,
	2: required string category,
	3: required string message,
	4: optional string level, 
	5: optional string phpInfo,
	6: optional string callStack,
	7: optional string request,
	8: optional string cookie,
	9: optional string wholy;
}

struct ResponseData {
	1: optional string id,
	2: optional i32 count;
	3: optional i32 createTime;
	4: optional i32 lastModify;
}

struct ResponseResult {
	1: required i32 code,
	2: optional string message,
	3: optional ResponseData data,
}



#
# service
#

service Sight {
	/** add log info, and get similar */
	ResponseResult add(1:required InputMsg inputMsg),

	/** clear all sampling */
	void clear(1: i32 deadline=0),

	/** set retire */
	void setRetire(1: required bool enabled, 2: required i32 timeOutSecondes, 3: required i32 intervalSecondes),

	/** get log info, use equals mode n.b.: not similar */
	ResponseResult get(1:required InputMsg inputMsg),

	/** get similar log info, n.b.: not add new sampling if not found similar */
	ResponseResult similar(1:required InputMsg inputMsg),

	/** remove sampling by equals */
	ResponseResult remove(1:required InputMsg inputMsg)

	/** get count*/
	i32 getCount()
	
	/** get size */
	i32 getSize()	

	/** dump data to file system */
	ResponseResult autodump(1: required bool enabled, 2: required i32 timeOutSecondes)
	
	/** dump data to file system */
	ResponseResult dump(1:optional string path)
	
	/** restore data from file system */
	ResponseResult restore(1:optional string path)
}
```


usage:
----------------
        
### run server:
    java ${JAVA_OPTS} -jar sight-1.0.0-jar-with-dependencies.jar ${Option}
    Option: -s --simple -m --multithread -n --nonblocking -t --threadedSelector
        option: 
		--simple, use TSimpleServer
		-s same to -simple
		--multithread, use TThreadPoolServer
		-m same to -multithread
		--nonblocking, use TNonblockingServer
            client must use TFramedTransport
		-n same to -nonblocking
		--threadedSelector, use TThreadedSelectorServer
            client must use TFramedTransport
		-t same to -threadedSelector

### java code gen
    thrift  --gen java service.thrift
    
        
### client usage in java code
```java
    TTransport transport = new TFramedTransport(new TSocket(this.host, this.port));
	//TTransport transport = new TSocket(this.host, this.port);
	transport.open();
	Sight.Client client = new Sight.Client(new TBinaryProtocol(transport));
	InputMsg inputMsg = new InputMsg();
	inputMsg.setId("abcd");
	inputMsg.setCategory("abcd");
	inputMsg.setMessage("this is a test");

	try {
		ResponseResult result = client.add(inputMsg);			
		this.transport.close();
	} catch (TException te) {
		if( te.getCause() instanceof SocketException) {
			te.getCause().printStackTrace();
			this.transport.close();
		}
		te.printStackTrace();
	} catch (Exception e) {
		e.printStackTrace();
	}        
```

Release
----------------
2013-08-28 by cy
1. project init

2013-08-28 18:21:15 by cy
1. 调整url处理的顺序为54=com.ganji.cateye.analyzer.UrlParameterTextFilter 
2. fix start server clear data bug