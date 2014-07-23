package com.ganji.cateye.flume;

import java.util.Random;

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
		if(now.getHourOfDay() != lastModifiy.getHourOfDay()) { // 过小时
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
			
		} else if (now.getMinuteOfHour() != lastModifiy.getMinuteOfHour()){ // 过分钟
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
		Stats stat = new Stats();
		Random rnd = new Random();
		for(int i=1; i<1000000; i++){
			stat.increase();
			if (i % 1000 == 0) {
				System.out.println(stat.toString() + " "  + i);
			}

			try {
				Thread.sleep(rnd.nextInt(2));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
