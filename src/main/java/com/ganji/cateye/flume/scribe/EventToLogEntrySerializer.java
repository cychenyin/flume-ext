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
package com.ganji.cateye.flume.scribe;

import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.conf.ComponentConfiguration;

import com.ganji.cateye.flume.scribe.thrift.LogEntry;

import java.nio.ByteBuffer;

public class EventToLogEntrySerializer implements FlumeEventSerializer {
    private String scribeCategoryHeaderKey;
    
    @Override
    public LogEntry serialize(Event event) {
        LogEntry entry = new LogEntry();
        entry.setMessage(ByteBuffer.wrap(event.getBody()));
        
        String category = event.getHeaders().get(scribeCategoryHeaderKey);
        if (category == null) {
            category = "empty";
        }

	try {
		System.out.println(new String(event.getBody(), "UTF-8"));
	} catch (Exception e) {
		e.printStackTrace();
	}

        entry.setCategory(category);
        return entry;
    }

    @Override
    public void close() {
    }

    @Override
    public void configure(Context context) {
        scribeCategoryHeaderKey = context.getString(ScribeSinkConstants.CONFIG_SCRIBE_CATEGORY_HEADER);
        if (scribeCategoryHeaderKey == null) {
            throw new RuntimeException(ScribeSinkConstants.CONFIG_SCRIBE_CATEGORY_HEADER + " is not configured.");
        }
    }

    @Override
    public void configure(ComponentConfiguration componentConfiguration) {
    }
}
