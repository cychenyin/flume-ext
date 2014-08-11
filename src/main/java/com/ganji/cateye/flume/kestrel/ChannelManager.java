package com.ganji.cateye.flume.kestrel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

public class ChannelManager {

	private List<Channel> channels = new ArrayList<Channel>();
	public List<Channel>	getChannels() {
		return channels;
	}

	/**
	 * load a total configuration from properties
	 * @param props
	 * @throws Exception 
	 */
	public void load(Properties props ) throws Exception {
		String routes = props.getProperty("routes");
		String[] arrRoute = routes.split(",");
		for( String route: arrRoute ) {
			ChannelBuilder cb = new ChannelBuilder();
			String prefix = "route." + route + ".";
			cb.setCategories( props.getProperty( prefix + "categories"));
			cb.setType( props.getProperty( prefix + "type"));
			for( Entry it : props.entrySet()) {
				String key = (String) it.getKey();
				if( key.startsWith(prefix)) {
					cb.setProperty( key.substring( prefix.length()), (String) it.getValue());
				}
			}
			Channel ch = cb.createChannel();
			channels.add( ch );
		}
		
		// 启动
		for( Channel ch : channels ) {
			ch.prepare();
		}
	}
	

	
	/**
	 * Iterator each definition and find the destination Channel
	 * @param catName
	 * @return
	 */
	public Channel getChannelByName(String catName) {
		for( Channel ch : channels ) {
			List<Category> r = ch.getCategoryList();
			for( Category category : r ) {
				if( category.match( catName ) ) {

					return ch;
				}
			}
		}
		return null;
	}
}
