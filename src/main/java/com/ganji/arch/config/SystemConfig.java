package com.ganji.arch.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Properties;

/**
 * auto generated by JavaConfigGen from system.config.properties
 * 
 * @author asdf
 * 
 */
public class SystemConfig implements Serializable {
	private SystemConfig() {
	}

	public final static String ResourceName = "system.config.properties";
	private static SystemConfig config;
	public static Properties props;

	public String Sms_Url;
	public String Sms_Content_Max;
	public String Mail_Url;
	public String Alert_Level_Error;
	public String Alert_Level_Fatal;
	public String System_Cateye_Url;
	public String System_Debug;
	public String System_Debug_Mail;
	public String System_Debug_Phone;
	public String Db_Mongo_Uri;
	public String Db_Mongo_Log_Batch_Size;
	public String Db_Mongo_Log;
	public String Db_Mongo_Log_All;
	public String Db_Mongo_Log_Newlog;
	public String Db_Mongo_Cateye;
	public String Db_Mongo_Cateye_Monitor;
	public String Db_Mongo_Cateye_Users;
	public String Db_Mongo_Cateye_Rules;
	public String Db_Mongo_Cateye_Slow_Sql;
	public String Db_Mongo_Cateye_Slow_Sql_Cache;
	public String Db_Mongo_Log_Alertrecords;
	public String Mongo_Uri;
	public String Memcached_Hosts;
	public String Threadpool_Parser_Core_Pool_Size;
	public String Threadpool_Parser_Max_Pool_Size;
	public String Threadpool_Parser_Keep_Alive_Time;
	public String Threadpool_Parser_Work_Queue_Size;
	public String Threadpool_Parser_Schedulepoolsize;
	public String Threadpool_Parser_Cache_Delay;
	public String Threadpool_Parser_Sleep;
	public String Threadpool_Coreprocessor_Core_Pool_Size;
	public String Threadpool_Coreprocessor_Max_Pool_Size;
	public String Threadpool_Coreprocessor_Keep_Alive_Time;
	public String Threadpool_Coreprocessor_Work_Queue_Size;
	public String Threadpool_Coreprocessor_Schedulepoolsize;
	public String Threadpool_Coreprocessor_Cache_Delay;
	public String Threadpool_Coreprocessor_Sleep;
	public String Scribe_Thrift_Maxworkerthreads;
	public String Scribe_Thrift_Minworkerthreads;
	public String Log_Ignore_Levels;
	public String Scribe_Server_Host;
	public String Scribe_Server_Port;
	public String Scribe_Server_Userlogs_Cache_Max;
	public String Cateye_Scheduler_Contacts_Interval;
	public String Statsd_Prefix;
	public String Slow_Sql_Statsd_Prefix;
	public String Statsd_Host;
	public String Statsd_Port;
	public String Cateye_Log_Level_Debug_Num;
	public String Cateye_Log_Level_Info_Num;
	public String Cateye_Log_Level_Warn_Num;
	public String Cateye_Log_Level_Error_Num;
	public String Cateye_Log_Level_Fatal_Num;
	public String Rta_Kestrel_Hosts;
	public String Rta_Kestrel_Port;
	public String Rta_Kestrel_Queue;
	public String Kafka_Broker_Host;
	public String Kafka_Spout_Zkhosts;
	public String Kafka_Spout_Zkport;
	public String Kafka_Spout_Zkroot;
	public String Topology_Workers;
	public String Topology_Debug;
	public String Topology_Acker_Executors;
	public String Topology_Message_Timeout_Secs;
	public String Topology_Kafkaspout_Parallelism;
	public String Topology_Parsebolt_Parallelism;
	public String Topology_Slowsqlbolt_Parallelism;
	public String Topology_Rulebolt_Parallelism;
	public String Topology_Alertbolt_Parallelism;
	public String Topology_Updatedbbolt_Parallelism;
	public String Topology_Statbolt_Parallelism;
	public String Cateye_Sight_Port;

	public static SystemConfig getInstance()
	{
		if (config == null)
		{
			synchronized (SystemConfig.class) {

				props = loadProperties(SystemConfig.ResourceName);
				SystemConfig temp = new SystemConfig();
				temp.Sms_Url = props.getProperty("sms.url");
				temp.Sms_Content_Max = props.getProperty("sms.content.max");
				temp.Mail_Url = props.getProperty("mail.url");
				temp.Alert_Level_Error = props.getProperty("alert.level.ERROR");
				temp.Alert_Level_Fatal = props.getProperty("alert.level.FATAL");
				temp.System_Cateye_Url = props.getProperty("system.cateye_url");
				temp.System_Debug = props.getProperty("system.debug");
				temp.System_Debug_Mail = props.getProperty("system.debug.mail");
				temp.System_Debug_Phone = props.getProperty("system.debug.phone");
				temp.Db_Mongo_Uri = props.getProperty("db.mongo.uri");
				temp.Db_Mongo_Log_Batch_Size = props.getProperty("db.mongo.log.batch.size");
				temp.Db_Mongo_Log = props.getProperty("db.mongo.log");
				temp.Db_Mongo_Log_All = props.getProperty("db.mongo.log.all");
				temp.Db_Mongo_Log_Newlog = props.getProperty("db.mongo.log.newlog");
				temp.Db_Mongo_Cateye = props.getProperty("db.mongo.cateye");
				temp.Db_Mongo_Cateye_Monitor = props.getProperty("db.mongo.cateye.monitor");
				temp.Db_Mongo_Cateye_Users = props.getProperty("db.mongo.cateye.users");
				temp.Db_Mongo_Cateye_Rules = props.getProperty("db.mongo.cateye.rules");
				temp.Db_Mongo_Cateye_Slow_Sql = props.getProperty("db.mongo.cateye.slow_sql");
				temp.Db_Mongo_Cateye_Slow_Sql_Cache = props.getProperty("db.mongo.cateye.slow_sql.cache");
				temp.Db_Mongo_Log_Alertrecords = props.getProperty("db.mongo.log.alertrecords");
				temp.Mongo_Uri = props.getProperty("mongo.uri");
				temp.Memcached_Hosts = props.getProperty("memcached.hosts");
				temp.Threadpool_Parser_Core_Pool_Size = props.getProperty("threadpool.parser.core_pool_size");
				temp.Threadpool_Parser_Max_Pool_Size = props.getProperty("threadpool.parser.max_pool_size");
				temp.Threadpool_Parser_Keep_Alive_Time = props.getProperty("threadpool.parser.keep_alive_time");
				temp.Threadpool_Parser_Work_Queue_Size = props.getProperty("threadpool.parser.work_queue_size");
				temp.Threadpool_Parser_Schedulepoolsize = props.getProperty("threadpool.parser.schedulePoolSize");
				temp.Threadpool_Parser_Cache_Delay = props.getProperty("threadpool.parser.cache.delay");
				temp.Threadpool_Parser_Sleep = props.getProperty("threadpool.parser.sleep");
				temp.Threadpool_Coreprocessor_Core_Pool_Size = props.getProperty("threadpool.coreprocessor.core_pool_size");
				temp.Threadpool_Coreprocessor_Max_Pool_Size = props.getProperty("threadpool.coreprocessor.max_pool_size");
				temp.Threadpool_Coreprocessor_Keep_Alive_Time = props.getProperty("threadpool.coreprocessor.keep_alive_time");
				temp.Threadpool_Coreprocessor_Work_Queue_Size = props.getProperty("threadpool.coreprocessor.work_queue_size");
				temp.Threadpool_Coreprocessor_Schedulepoolsize = props.getProperty("threadpool.coreprocessor.schedulePoolSize");
				temp.Threadpool_Coreprocessor_Cache_Delay = props.getProperty("threadpool.coreprocessor.cache.delay");
				temp.Threadpool_Coreprocessor_Sleep = props.getProperty("threadpool.coreprocessor.sleep");
				temp.Scribe_Thrift_Maxworkerthreads = props.getProperty("scribe.thrift.maxWorkerThreads");
				temp.Scribe_Thrift_Minworkerthreads = props.getProperty("scribe.thrift.minWorkerThreads");
				temp.Log_Ignore_Levels = props.getProperty("log.ignore.levels");
				temp.Scribe_Server_Host = props.getProperty("scribe.server.host");
				temp.Scribe_Server_Port = props.getProperty("scribe.server.port");
				temp.Scribe_Server_Userlogs_Cache_Max = props.getProperty("scribe.server.userlogs.cache.max");
				temp.Cateye_Scheduler_Contacts_Interval = props.getProperty("cateye.scheduler.contacts.interval");
				temp.Statsd_Prefix = props.getProperty("statsd.prefix");
				temp.Slow_Sql_Statsd_Prefix = props.getProperty("slow_sql.statsd.prefix");
				temp.Statsd_Host = props.getProperty("statsd.host");
				temp.Statsd_Port = props.getProperty("statsd.port");
				temp.Cateye_Log_Level_Debug_Num = props.getProperty("cateye.log.level.debug.num");
				temp.Cateye_Log_Level_Info_Num = props.getProperty("cateye.log.level.info.num");
				temp.Cateye_Log_Level_Warn_Num = props.getProperty("cateye.log.level.warn.num");
				temp.Cateye_Log_Level_Error_Num = props.getProperty("cateye.log.level.error.num");
				temp.Cateye_Log_Level_Fatal_Num = props.getProperty("cateye.log.level.fatal.num");
				temp.Rta_Kestrel_Hosts = props.getProperty("rta.kestrel.hosts");
				temp.Rta_Kestrel_Port = props.getProperty("rta.kestrel.port");
				temp.Rta_Kestrel_Queue = props.getProperty("rta.kestrel.queue");
				temp.Kafka_Broker_Host = props.getProperty("kafka.broker.host");
				temp.Kafka_Spout_Zkhosts = props.getProperty("kafka.spout.zkhosts");
				temp.Kafka_Spout_Zkport = props.getProperty("kafka.spout.zkport");
				temp.Kafka_Spout_Zkroot = props.getProperty("kafka.spout.zkroot");
				temp.Topology_Workers = props.getProperty("topology.workers");
				temp.Topology_Debug = props.getProperty("topology.debug");
				temp.Topology_Acker_Executors = props.getProperty("topology.acker.executors");
				temp.Topology_Message_Timeout_Secs = props.getProperty("topology.message.timeout.secs");
				temp.Topology_Kafkaspout_Parallelism = props.getProperty("topology.kafkaspout.parallelism");
				temp.Topology_Parsebolt_Parallelism = props.getProperty("topology.parsebolt.parallelism");
				temp.Topology_Slowsqlbolt_Parallelism = props.getProperty("topology.slowsqlbolt.parallelism");
				temp.Topology_Rulebolt_Parallelism = props.getProperty("topology.rulebolt.parallelism");
				temp.Topology_Alertbolt_Parallelism = props.getProperty("topology.alertbolt.parallelism");
				temp.Topology_Updatedbbolt_Parallelism = props.getProperty("topology.updatedbbolt.parallelism");
				temp.Topology_Statbolt_Parallelism = props.getProperty("topology.statbolt.parallelism");
				temp.Cateye_Sight_Port = props.getProperty("cateye.sight.port");

				config = temp;
			}
		}
		return config;
	}

	public static Properties loadProperties(String fileName) {
		Properties props = null;
		InputStream stream = null;

		try {
			stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
			props = new Properties();
			props.load(stream);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
				}
			}
		}

		return props;
	}
}
