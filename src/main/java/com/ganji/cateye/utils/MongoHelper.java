package com.ganji.cateye.utils;

import java.io.Serializable;
import java.net.UnknownHostException;
import java.util.Date;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ganji.arch.config.SystemConfig;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import com.mongodb.MongoURI;

public class MongoHelper implements Serializable {
	private Logger logger = LoggerFactory.getLogger(MongoHelper.class);
	private String uri;

	private transient Mongo mongo;
	/**
	 * mongo是否可用
	 */
	private boolean inService = true;

	public MongoHelper() {
		this.uri = SystemConfig.getInstance().Db_Mongo_Uri;
		this.init();
	}

	// @PostConstruct
	public void init() {
		try {
			// if (StringUtils.isNotBlank(uri))
			mongo = new Mongo(new MongoURI(uri));
			/*else
				mongo = new Mongo(host, port);*/
		} catch (UnknownHostException e) {
			e.printStackTrace();
			if (logger.isErrorEnabled()) {
				logger.error("mongo fail to init:" + e.getMessage(), e);
			}
			inService = false;
			System.exit(-1);
		} catch (Exception e) {
			e.printStackTrace();
			if (logger.isErrorEnabled()) {
				logger.error("mongo fail to init:" + e.getMessage(), e);
			}
			inService = false;
		}
	}

	/**
	 * 获取mongodb实例，mongo本身是线程安全的，本身就实现了池，不需要关闭
	 * 
	 * @return
	 */
	public Mongo getMongo() {
		if(mongo == null) {
			this.init();
		}
		return mongo;
	}

	/**
	 * 获取db 的连接
	 * 
	 * @param mongo
	 * @param dbname
	 * @param colName
	 * @return
	 */
	public DBCollection getDbConCollection(String dbname, String colName) {
		Mongo mongo = getMongo();
		DB db = mongo.getDB(dbname);

		return db.getCollection(colName);
	}

	/**
	 * 关闭mongodb
	 * 
	 * @param mongo
	 */
	public void close(Mongo mongo) {
		mongo.close();
	}

	/**
	 * 关闭连接，重新连
	 * 
	 * @return
	 */
	public Mongo resetMongo() {
		if (mongo != null) {
			mongo.close();
			mongo = null;
		}

		init();
		mongo = getMongo();
		inService = true;

		return mongo;
	}

	public boolean isInService() {
		return inService;
	}

	public void setInService(boolean inService) {
		this.inService = inService;
	}

}
