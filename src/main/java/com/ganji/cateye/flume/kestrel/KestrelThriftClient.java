package com.ganji.cateye.flume.kestrel;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.lag.kestrel.thrift.Item;
import net.lag.kestrel.thrift.Kestrel;
import net.lag.kestrel.thrift.QueueInfo;
import net.lag.kestrel.thrift.Status;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

public class KestrelThriftClient implements Kestrel.Iface {
    Kestrel.Client _client = null;
    TTransport _transport = null;

    public KestrelThriftClient(String hostname, int port)
        throws TException {

        _transport = new TFramedTransport(new TSocket(hostname, port));
        TProtocol proto = new TBinaryProtocol(_transport);
        _client = new Kestrel.Client(proto);
        _transport.open();
    }

    public void close() {
        _transport.close();
        _transport = null;
        _client = null;
    }

    public QueueInfo peek(String queue_name) throws TException {
        return _client.peek(queue_name);
    }

    public void delete_queue(String queue_name) throws TException {
        _client.delete_queue(queue_name);
    }

    public String get_version() throws TException {
        return _client.get_version();
    }

    @Override
    public int put(String queue_name, List<ByteBuffer> items, int expiration_msec) throws TException {
        return _client.put(queue_name, items, expiration_msec);
    }
    
    public void put(String queue_name, String item, int expiration_msec) throws TException {
        List<ByteBuffer> toPut = new ArrayList<ByteBuffer>();
        try {
            toPut.add(ByteBuffer.wrap(item.getBytes("UTF-8")));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        put(queue_name, toPut, expiration_msec);
    }

    @Override
    public List<Item> get(String queue_name, int max_items, int timeout_msec, int auto_abort_msec) throws TException {
        return _client.get(queue_name, max_items, timeout_msec, auto_abort_msec);
    }

    @Override
    public int confirm(String queue_name, Set<Long> ids) throws TException {
        return _client.confirm(queue_name, ids);
    }

    @Override
    public int abort(String queue_name, Set<Long> ids) throws TException {
        return _client.abort(queue_name, ids);
    }

    @Override
    public void flush_queue(String queue_name) throws TException {
        _client.flush_queue(queue_name);
    }

    @Override
    public void flush_all_queues() throws TException {
        _client.flush_all_queues();
    }

	@Override
	public Status current_status() throws TException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void set_status(Status status) throws TException {
		// TODO Auto-generated method stub
		
	}
}

