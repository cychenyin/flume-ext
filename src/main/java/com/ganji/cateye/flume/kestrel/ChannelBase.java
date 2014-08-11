package com.ganji.cateye.flume.kestrel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import scribe.thrift.LogEntry;

public abstract class ChannelBase implements Channel{
	private String type=null;
	private String categoies;
	protected List<Category> listCategory;
	private String filters=null;
	protected List<IMessageFilter> listFilters;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	public void setCategories(String categories) {
		this.categoies = categories;

		listCategory = new ArrayList<Category>();
		for( String catName : categoies.split(" ") ) {
			listCategory.add( new Category(catName) ); 
		}
		
		Collections.sort(
				listCategory, new Comparator<Category>() {

					@Override
					public int compare(Category arg0, Category arg1) {						
						if( arg1.getBlock() && !arg0.getBlock()) return 1;
						if( !arg1.getBlock() && arg0.getBlock()) return -1;
						return 0;
					}
					
				});
	}
	
	public String getCategories() {
		return "";
	}
	
	@Override
	public List<Category> getCategoryList() {
		return listCategory;
	}

	public String getFilters() {
		return filters;
	}

	public void setFilters(String filters) {
		this.filters = filters;
		
		listFilters = new ArrayList<IMessageFilter>();
		for( String clsName : filters.split(",") ) {
			try {
				Class cls = Class.forName(clsName);
				listFilters.add( (IMessageFilter) cls.newInstance()  ); 
			} catch(ClassNotFoundException e) {
				
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void write(List<LogEntry> messages, boolean endOfBatch) {
		if(  listFilters == null || listFilters.isEmpty() ) {
			_write(messages, endOfBatch);
			return;
		}
		// process and write
		List<LogEntry> msgs = new ArrayList<LogEntry>();
		for( LogEntry msg: messages ) {
			LogEntry l = msg;
			for( IMessageFilter filter : listFilters) {
				l = filter.process(l);
				if( l == null ) break;
			}
			if( l != null )
				msgs.add(l);
		}
		_write(msgs, endOfBatch);
	}
	
	public abstract void _write(List<LogEntry> messages, boolean endOfBatch);
	
	public boolean isMatchCategory(String category) {
		for(Category c : listCategory) {
			if( c.getBlock() && c.match(category)) return false; // block are processed first
			if( !c.getBlock() && c.match(category)) return true; // then permit list
		}
		// default block
		return false;
	}
}
