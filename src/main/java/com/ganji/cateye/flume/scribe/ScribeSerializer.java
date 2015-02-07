package com.ganji.cateye.flume.scribe;

import java.nio.ByteBuffer;

import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.transport.TMemoryBuffer;
import org.apache.thrift.transport.TMemoryInputTransport;
import org.apache.thrift.transport.TTransport;

import com.ganji.cateye.flume.kestrel.KestrelSinkConsts;
//import com.ganji.cateye.flume.scribe.thrift.LogEntry;
import org.apache.flume.source.scribe.*;
import com.google.common.base.Charsets;

public class ScribeSerializer implements MessageSerializer {

	protected String categoryHeaderKey = "category";

	/**
	 * serialize the logEntry into a ByteBuffer, which is required by kestrel
	 *  
	 * @param log
	 * @return
	 */
	public ByteBuffer encodeToByteBuffer(LogEntry log, boolean compress) {
		TMemoryBuffer buffer = new TMemoryBuffer(1024);
		try {
			log.write(new TCompactProtocol(buffer));
		} catch (TException e) {
			e.printStackTrace();
			return null;
		}
		
		int len = buffer.length();
		ByteBuffer out = ByteBuffer.allocate(len);
		out.put(buffer.getArray(), buffer.getBufferPosition(), len);
		out.flip();
		return out;
	}

	public static LogEntry decodeFromByteBuffer(ByteBuffer buffer) {
		TTransport buf = new TMemoryInputTransport(buffer.array());
		LogEntry log = new LogEntry();
		try {
			log.read(new TCompactProtocol(buf));
		} catch (TException e) {
			e.printStackTrace();
			return null;
		}
		return log;
	}

	@Override
	public void configure(Context context) {
		categoryHeaderKey = context.getString(KestrelSinkConsts.CONFIG_CATEGORY_HEADER,
				KestrelSinkConsts.DEFAULT_CATEGORY_HEADER);
	}

	@Override
	public LogEntry serialize(Event event) {
		LogEntry entry = new LogEntry();
		// code for thrift 0.8+
		// entry.setMessage(ByteBuffer.wrap(event.getBody()));
		// code for thrift 0.7
		entry.setMessage( new String(event.getBody(), Charsets.UTF_8));
		
		String category = event.getHeaders().get(this.categoryHeaderKey);
		if (category == null) {
			category = "empty";
		}

		entry.setCategory(category);
		return entry;
	}

}
