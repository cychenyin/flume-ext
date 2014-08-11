package com.ganji.cateye.flume.kestrel;

/**
 * 匹配LogEntry的category，支持通配符*，但是只能在最后面
 * @author tailor
 *
 */
public class Category {
	private String name;
	private String prefix;
	private boolean wildMatch;
	private boolean block = false;
	
	public Category(String name) {
		this.name = name;
		if( name.startsWith("^")) {
			this.name = name.substring(1,name.length());
			name = this.name;
			this.block = true;
		}
		if( name.endsWith("*")) {
			prefix = name.substring(0,name.length()-1);
			wildMatch = true;
		}
	}
	
	public boolean match( String tester ) {
		if( tester == null )
			return false;
		if( wildMatch ) {
			return tester.startsWith(prefix);
		}
		return name.equals(tester);
	}
	
	public boolean getBlock() {
		return block;
	}
}
