package com.ganji.cateye.flume;

import org.joda.time.DateTime;

public class Stats {
	
	long[] seconds = new long[60];
	long[] minutes = new long[60];
	long[] hours = new long[24];


	public void increase(){
		increase(1L);
	}
	DateTime lastModifiy = DateTime.now();
	public synchronized void increase(long i){
		DateTime now = DateTime.now();
		long diff = now.getMillis() - lastModifiy.getMillis();
		if(diff > 3600000) { // 过小时
			// 汇总分钟
			int minute = lastModifiy.getMinuteOfHour();
			for(int t = 0; t< 60; t++){
				this.minutes[minute] += seconds[t];
				seconds[t] = 0;
			}
			// 汇总小时
			int hour = lastModifiy.getHourOfDay();
			for(int t = 0; t< 60; t++){
				this.hours[hour] += minutes[t];
				minutes[t] = 0;
			}
			
		} else if (diff > 60000){ // 过分钟
			// 汇总分钟
			int minute = lastModifiy.getMinuteOfHour();
			for(int t = 0; t< 60; t++){
				this.minutes[minute] += seconds[t];
				seconds[t] = 0;
			}
		} 
		
		this.seconds[now.getSecondOfMinute()] += i;
		
		lastModifiy = now;
	}
	
//	private long total() {
//		
//	}
	public String toString(){
		StringBuilder sb  = new StringBuilder();
		long sum = 0;
		for(int i=0;i<60;i++) {
			sum += this.seconds[i];
		}
		sb.append("speed=" );
		sb.append(sum/60);
 		sb.append("\t");

		for(int i=0;i<60; i++) {
			sb.append(this.minutes[i]);
			sb.append(" ");
		}

		return sb.toString();
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
