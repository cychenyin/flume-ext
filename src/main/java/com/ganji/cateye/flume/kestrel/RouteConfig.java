/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
/*
 * create: asdf
 * create datetime: 2015-02-06 16:17:16 
 * 
 * */

package com.ganji.cateye.flume.kestrel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

public class RouteConfig {

	public final static String SPLITTER = "[\\s|,;]";
	// map kv = (cateogry, queueName); 其中category支持*通配符结尾， 且不移除通配符
	private Map<String, String> routes;
	private Set<String> wildcardCategories; // 其中不含*， 已被移除

	public RouteConfig() {
		routes = new HashMap<String, String>();
		wildcardCategories = new HashSet<String>();
	}

	// categories will be split
	public void add(String categories, String queue) {
		String[] ary = categories.split(RouteConfig.SPLITTER);
		for (String c : ary) {
			if (c.isEmpty())
				continue;
			if (c.endsWith("*")) {
				wildcardCategories.add(c.substring(0, c.length() - 1));
			}
			routes.put(c, queue);
		}
	}

	// find kestrel queue name thought category
	public String route(String category) {
		String ret = routes.get(category);
		if (StringUtils.isEmpty(ret)) {
			for (String w : wildcardCategories) {
				if (category.startsWith(w)) {
					ret = routes.get(w + "*");
					break;
				}
			}
		}
		return ret;
	}
	
	// clear
	public void clear() {
		this.routes.clear();
		this.wildcardCategories.clear();
	}
}
