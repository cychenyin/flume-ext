package com.ganji.cateye.flume.kestrel;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 配置实例
 * create a channel topology
 * routes=XXXX,YYYY
 * route.XXXX.categories=ms.pv ms.ev ms.*
 * route.XXXX.channel=kestrel
 * route.XXXX.channel.host=
 * route.XXXX.channel.port=
 * route.YYYY. ...
 * 
 * @author tailor
 */
public class ChannelBuilder {
	private static Logger LOGGER = LoggerFactory.getLogger(ChannelBuilder.class);
	
	private String categories;
	private String type;
	private HashMap<String,String> properties = new HashMap<String,String>();
	public void setCategories(String categories) {
		this.categories = categories;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setProperty(String key, String value) {
		properties.put(key, value);
	}

	public Channel createChannel() {
		Channel ch;
		if( type.equals("kestrel")) {
			ch = new KestrelChannel();
		}
//		else if ( type.equals("scribe")) {
//			ch = new ScribeChannel();
//		}
//		else if ( type.equals("stdout")) {
//			ch = new StdOutChannel();
//		}
//		else if ( type.equals("log")) {
//			ch = new LogChannel();
//		}
		else
			throw new RuntimeException("Unknown type of channel " + type);
		
		// apply properties
		for( Map.Entry<String,String> entry : properties.entrySet()) {
			String name = "set" + 
					entry.getKey().substring(0,1).toUpperCase() + 
					entry.getKey().substring(1);
			try {
				Method f = ch.getClass().getMethod(name, String.class);
				f.invoke( ch, entry.getValue() );
				
			} catch (SecurityException e) {
				LOGGER.error("Error apply property to" + name, e);
			} catch (NoSuchMethodException e) {
				LOGGER.error("Error apply property to" + name, e);
			} catch (IllegalArgumentException e) {
				LOGGER.error("Error apply property to" + name, e);
			} catch (IllegalAccessException e) {
				LOGGER.error("Error apply property to" + name, e);
			} catch (InvocationTargetException e) {
				LOGGER.error("Error apply property to" + name, e);
			}
		}

		return ch;
	}

}
