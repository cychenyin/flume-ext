package com.ganji.arch.config;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 系统配置
 * @author hexiaohua
 *
 */
@Component("systemConfigHelper")
public class SystemConfigHelper {
	/**
	 * 系统是否处于调试阶段
	 */
	@Value("#{systemConfigProps['system.debug']}")
	private  boolean debug;
	@Autowired
	@Qualifier("systemConfigProps")
	private Properties properties;
	
	
	public  int getInt(String name) {
		return Integer.parseInt(properties.getProperty(name));
	}
	
	public  boolean getBoolean(String name) {
		return Boolean.parseBoolean(properties.getProperty(name));
	}
	
	public  long getLong(String name) {
		return Long.parseLong(properties.getProperty(name));
	}
	
	public String getString(String name) {
		return properties.getProperty(name);
	}
	
	public  boolean isDebug() {
		return debug;
	}
}
