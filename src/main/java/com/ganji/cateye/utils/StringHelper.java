package com.ganji.cateye.utils;

import java.util.List;

import org.apache.commons.lang.StringUtils;

public final class StringHelper {
	
	public static double tryParseDouble(String val, double defaultValue) {
		if(StringUtils.isEmpty(val)) 
			return defaultValue;
		try {
			return Double.parseDouble(val);
		} catch (Exception e) {

			return defaultValue;
		}
	}
	public static int tryParseInt(String val, int defaultValue) {
		if(StringUtils.isEmpty(val))
			return defaultValue;
		try {
			return (int)Double.parseDouble(val);
		} catch (Exception e) {

			return defaultValue;
		}
	}
	public static boolean tryParseBoolean(String val, boolean defaultValue) {
		if(StringUtils.isEmpty(val)) 
			return defaultValue;
		try {
			return Boolean.parseBoolean(val);
		} catch (Exception e) {

			return defaultValue;
		}
	}
	
	public static String join(String seperator, List<String> list) {
    	if (list == null) return null;
    	if (seperator == null) seperator = "";
    	StringBuilder sb = new StringBuilder();
    	for(int i = 0;i<list.size();i++) {
    		sb.append(list.get(i));
    		if(i < list.size()-1) {
    			sb.append(seperator);
    		}
    	}
    	return sb.toString();
    }
    public static String join(String seperator, String[] ary) {
    	if (ary == null) return null;
    	if (seperator == null) seperator = "";
    	StringBuilder sb = new StringBuilder();
    	for(int i = 0;i<ary.length;i++) {
    		sb.append(ary[i]);
    		if(i < ary.length-1) {
    			sb.append(seperator);
    		}
    	}
    	return sb.toString();
    }
}
