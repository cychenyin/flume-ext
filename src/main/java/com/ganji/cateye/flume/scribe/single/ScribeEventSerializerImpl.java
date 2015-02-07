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
package com.ganji.cateye.flume.scribe.single;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.conf.ComponentConfiguration;

//import com.ganji.cateye.flume.scribe.ScribeSinkConsts;
//import com.ganji.cateye.flume.scribe.thrift.LogEntry;
import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import org.apache.flume.source.scribe.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
public class ScribeEventSerializerImpl implements ScribeEventSerializer {
    private static final Logger logger = LoggerFactory.getLogger(ScribeEventSerializerImpl.class.getName());
	
	private String scribeCategoryHeaderKey;

	@Override
	public LogEntry serialize(Event event) {
		LogEntry entry = new LogEntry();
		// code for thrift 0.8 +
		// entry.setMessage(ByteBuffer.wrap(event.getBody()));
		entry.setMessage(new String(event.getBody(), Charsets.UTF_8));
		// code of thrift 0.7
		String category = event.getHeaders().get(scribeCategoryHeaderKey);
		if (category == null) {
			category = "empty";
		}

		entry.setCategory(category);
		return entry;
	}

	@Override
	public void close() {
	}

	@Override
	public void configure(Context context) {
		scribeCategoryHeaderKey = context.getString(ScribeSinkConsts.CONFIG_CATEGORY_HEADER_KEY);
		if (scribeCategoryHeaderKey == null) {
			throw new RuntimeException(ScribeSinkConsts.CONFIG_CATEGORY_HEADER_KEY + " is not configured.");
		}
	}

	@Override
	public void configure(ComponentConfiguration componentConfiguration) {
	}
}
