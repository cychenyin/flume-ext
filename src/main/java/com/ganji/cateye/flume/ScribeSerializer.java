package com.ganji.cateye.flume;

import java.nio.ByteBuffer;

import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.transport.TMemoryBuffer;
import org.apache.thrift.transport.TMemoryInputTransport;
import org.apache.thrift.transport.TTransport;

import com.ganji.cateye.flume.kestrel.KestrelSinkConstants;
import com.ganji.cateye.flume.scribe.thrift.LogEntry;

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
		categoryHeaderKey = context.getString(KestrelSinkConstants.CONFIG_CATEGORY_HEADER,
				KestrelSinkConstants.CONFIG_CATEGORY_HEADER_DEFAULT);
	}

	@Override
	public LogEntry serialize(Event event) {
		LogEntry entry = new LogEntry();
		entry.setMessage(ByteBuffer.wrap(event.getBody()));

		String category = event.getHeaders().get(this.categoryHeaderKey);
		if (category == null) {
			category = "empty";
		}

		entry.setCategory(category);
		return entry;
	}

}
