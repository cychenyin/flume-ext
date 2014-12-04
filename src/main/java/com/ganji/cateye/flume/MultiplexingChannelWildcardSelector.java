package com.ganji.cateye.flume;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.flume.Channel;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.FlumeException;
import org.apache.flume.channel.AbstractChannelSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 分叉流通配选择器
 * 	NXX的名字，呵呵， 容易产生不明觉厉的赶脚
 * 要点：
 * 实现支持*通配符后缀的Selector，用于Source选择Channels， *只允许放在最后； 
 * 同一个category只会命中最多一个channel
 * 优先匹配category最长的channel
 * @author asdf
 * 
 * example:
 * agent.sources.appSrc.selector.type = com.ganji.cateye.flume.MultiplexingChannelWildcardSelector
 * agent.sources.appSrc.selector.header = category
 * agent.sources.appSrc.selector.mapping.uc.log.op.* = c5 c7 c8
 * agent.sources.appSrc.selector.mapping.uc.log.* = c7 c8
 * agent.sources.appSrc.selector.mapping.ms.pv = cpv
 * agent.sources.appSrc.selector.optional.cdc.data.* = c4
 * agent.sources.appSrc.selector.default = c1
 */
public class MultiplexingChannelWildcardSelector extends AbstractChannelSelector {

	public static final String CONFIG_MULTIPLEX_HEADER_NAME = "header";
	public static final String DEFAULT_MULTIPLEX_HEADER = "flume.selector.header";
	public static final String CONFIG_PREFIX_MAPPING = "mapping.";
	public static final String CONFIG_DEFAULT_CHANNEL = "default";
	public static final String CONFIG_PREFIX_OPTIONAL = "optional";

	@SuppressWarnings("unused")
	private static final Logger LOG = LoggerFactory.getLogger(MultiplexingChannelWildcardSelector.class);
	private static final List<Channel> EMPTY_LIST = Collections.emptyList();
	private String headerName;
	private Map<String, List<Channel>> channelMapping;
	private List<String> wildcardKeyOfMapping; // 其中存储的时候*前面的内容
	private Map<String, List<Channel>> optionalChannels;
	private List<String> wildcardKeyOfOptional;
	private List<Channel> defaultChannels;

	@SuppressWarnings("unused")
	private List<Channel> selectChannel(Map<String, List<Channel>> channels, List<String> wildcards, String headerValue) {
		headerValue = headerValue.trim();
		List<Channel> ret = channels.get(headerValue);

		if (ret == null) {
			for (String wildcard : wildcards) {
				if (headerValue.startsWith(wildcard)) {
					ret = channels.get(wildcard + "*");
					break;
				}
			}
		}
		return ret;
	}

	@Override
	public List<Channel> getRequiredChannels(Event event) {
		String headerValue = event.getHeaders().get(headerName);
		if (headerValue == null || headerValue.trim().length() == 0) {
			return defaultChannels;
		}
		// List<Channel> channels = channelMapping.get(headerValue);
		List<Channel> channels = selectChannel(channelMapping, wildcardKeyOfMapping, headerValue);
		// This header value does not point to anything
		// Return default channel(s) here.
		if (channels == null) {
			channels = defaultChannels;
		}
		return channels;
	}

	@Override
	public List<Channel> getOptionalChannels(Event event) {
		String headerValue = event.getHeaders().get(headerName);
		// List<Channel> channels = optionalChannels.get(headerValue);
		List<Channel> channels = selectChannel(optionalChannels, wildcardKeyOfOptional, headerValue);
		if (channels == null) {
			channels = EMPTY_LIST;
		}
		return channels;
	}

	@Override
	public void configure(Context context) {
		this.headerName = context.getString(CONFIG_MULTIPLEX_HEADER_NAME, DEFAULT_MULTIPLEX_HEADER);
		Map<String, Channel> channelNameMap = getChannelNameMap();
		defaultChannels = getChannelListFromNames(context.getString(CONFIG_DEFAULT_CHANNEL), channelNameMap);
		Map<String, String> mapConfig = context.getSubProperties(CONFIG_PREFIX_MAPPING);
		channelMapping = new HashMap<String, List<Channel>>();
		wildcardKeyOfMapping = new ArrayList<String>();

		for (String headerValue : mapConfig.keySet()) {
			List<Channel> configuredChannels = getChannelListFromNames(mapConfig.get(headerValue), channelNameMap);

			// This should not go to default channel(s)
			// because this seems to be a bad way to configure.
			if (configuredChannels.size() == 0) {
				throw new FlumeException("No channel configured for when header value is: " + headerValue);
			}
			if (channelMapping.put(headerValue, configuredChannels) != null) {
				throw new FlumeException("Selector channel configured twice");
			}
			if (headerValue.endsWith("*")) {
				wildcardKeyOfMapping.add(headerValue.substring(0, headerValue.length() - 1));
			}
		}
		descSortByLength(wildcardKeyOfMapping);
		
		// If no mapping is configured, it is ok.
		// All events will go to the default channel(s).
		Map<String, String> optionalChannelsMapping = context.getSubProperties(CONFIG_PREFIX_OPTIONAL + ".");

		optionalChannels = new HashMap<String, List<Channel>>();
		wildcardKeyOfOptional = new ArrayList<String>();
		for (String hdr : optionalChannelsMapping.keySet()) {
			List<Channel> confChannels = getChannelListFromNames(optionalChannelsMapping.get(hdr), channelNameMap);
			if (confChannels.isEmpty()) {
				confChannels = EMPTY_LIST;
			}
			// Remove channels from optional channels, which are already configured to be required channels.
			List<Channel> reqdChannels = channelMapping.get(hdr);
			// Check if there are required channels, else defaults to default channels
			if (reqdChannels == null || reqdChannels.isEmpty()) {
				reqdChannels = defaultChannels;
			}
			for (Channel c : reqdChannels) {
				if (confChannels.contains(c)) {
					confChannels.remove(c);
				}
			}

			if (optionalChannels.put(hdr, confChannels) != null) {
				throw new FlumeException("Selector channel configured twice");
			}
			if (hdr.endsWith("*")) {
				wildcardKeyOfOptional.add(hdr.substring(0, hdr.length() - 1));
			}
		}
		descSortByLength(wildcardKeyOfOptional);
	}
	
	// 根据字串符长度倒序排序；如果尾部有*号，则不考虑型号
	private void descSortByLength(List<String> list) {
		Collections.sort(list, new Comparator<String>(){
			
			@Override
			public int compare(String o1, String o2) {
				if(o1 == null || o2 == null )
					return -1;
				int l2 = o2.endsWith("*") ? o2.length() - 1 : o2.length();
				int l1 = o1.endsWith("*") ? o1.length() - 1 : o1.length();
				return l2 - l1;
			}});

	}
}
