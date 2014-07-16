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

