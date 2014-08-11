package com.ganji.cateye.flume.kestrel;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import scribe.thrift.LogEntry;

/**
 * 支持两种序列化策略，固定的category，以及按照输入的category
 * currently, only 1 connection is supported, while SpyMemcached is async
 * the performance is good enough. 
 * see: http://stackoverflow.com/questions/8683142/memcached-client-opening-closing-and-reusing-connections
 * @author tailor
 *
 */
public class KestrelChannel extends ChannelBase {
	private static Logger LOGGER = LoggerFactory.getLogger(KestrelChannel.class);
	
	private KestrelThriftClient client;
	private String forceCategory = null;
	private String addr=null;
	private boolean compress = false;
	private String serializerId = "scribe";

//	private long currentTimestamp = 0;
//	private long countThisSecond = 0;
	private long maxPerSecond = 10000;
	private final int RETRY_INTERVAL = 1*1000;
	private List<ByteBuffer> pendingItems = new LinkedList<ByteBuffer>();
	private KestrelSerializer	serializer = null;

	/**
	 * 当发生网络故障时，决定下次重连的时间
	 */
	private long reconnectNext;
	
	/**
	 * 
	 * @param addr 支持多个地址server1:11211 server2:11211
	 * @param forceCategory 是否强制使用固定的category
	 * @throws IOException
	 * @throws TException 
	 * @throws NumberFormatException 
	 */
	public void prepare() throws IOException, NumberFormatException, TException {
		initThriftClient();
		
		if( serializerId.equalsIgnoreCase("scribe")) {
			serializer = new ScribeSerializer();
		}
		else if( serializerId.equalsIgnoreCase("plain-message") ) {
			serializer = new PlainMessageSerializer();
		}
		else 
			throw new RuntimeException("invalid serializer specified");
	}

	private void initThriftClient() throws NumberFormatException, TException {
		if( LOGGER.isInfoEnabled()) 
			LOGGER.info("connecting to kestrel:{}",addr );
		String[] addrs = addr.split(":");
		client = new KestrelThriftClient(addrs[0], Integer.parseInt( addrs[1] ));		
	}

	public String getAddr() {
		return addr;
	}

	public void setAddr(String addr) {
		this.addr = addr;
	}

	public String getForceCategory() {
		return forceCategory;
	}

	public void setForceCategory(String forceCategory) {
		this.forceCategory = forceCategory;
	}

	public String getCompress() {
		return ""+compress;
	}

	public void setCompress(String compress) {
		this.compress = compress.equals("true") || compress.equals("1") || compress.equals("yes");
	}

	public long getMaxPerSecond() {
		return maxPerSecond;
	}

	public void setMaxPerSecond(String maxPerSecond) {
		this.maxPerSecond = Integer.parseInt(maxPerSecond);
	}

	@Override
	public void _write(List<LogEntry> messages, boolean endOfBatch) {
		for( LogEntry msg : messages ) {
			if( isMatchCategory( msg.getCategory() )) {
				ByteBuffer buf = serializer.encodeToByteBuffer(msg, false);
				synchronized( pendingItems ) {
					pendingItems.add( buf );
				}
			}
			else {
				if( LOGGER.isInfoEnabled() )
					LOGGER.info( "unmatched message , category="+msg.getCategory());
			}
		}
		if( endOfBatch ) {
			// do a batch write
			synchronized( pendingItems ) {
				flushPending();
			}
		}
	}

	/**
	 * flush pendingItem and clear buffer,
	 * reconnect if network error
	 */
	private void flushPending() {
		while(true) {
			// 如果连接不存在，则需要重建连接
			while( client == null ) {
				while( reconnectNext > System.currentTimeMillis() ) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
					}
				}
				try {
					initThriftClient();
				} catch (NumberFormatException e1) {	
					// 由于已经使用过，因此不会出现这个错误
				} catch (TException e1) {
					// 记录日志，并延迟重连
					LOGGER.error("reconnect to kestrel server <{}> failed. {}" , addr, e1.getMessage());
					reconnectNext = System.currentTimeMillis() + RETRY_INTERVAL;
				}
			}
			
			try {
				client.put( forceCategory, pendingItems, 0);
				if( LOGGER.isInfoEnabled() ) 
					LOGGER.info( "successfully send <{}> messages to remote kestrel server <{}>", pendingItems.size() , addr);
				pendingItems.clear();
				break;
			} catch (TException e) {
				LOGGER.error("failed to send <{}> messages to remote kestrel server <{}>, retry in {} seconds", pendingItems.size() , addr, RETRY_INTERVAL);
				client.close();
				client = null;
				reconnectNext = System.currentTimeMillis() + RETRY_INTERVAL;
			}
		}
	}

	public String getSerializerId() {
		return serializerId;
	}

	public void setSerializerId(String serializerId) {
		this.serializerId = serializerId;
	}

}
