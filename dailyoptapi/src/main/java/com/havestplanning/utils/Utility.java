package com.havestplanning.utils;

import java.util.Calendar;
import java.util.Date;

import com.havestplanning.model.QualityFunction;

public class Utility {

	public static double getInducedValue(int x1, double y1, int x2, double y2, int x){
		if(x1 > x2){
			int tmp = x1; x1 = x2; x2 = tmp;
			double tmpf = y1; y1 = y2; y2 = tmpf;
		}
		if(x <= x1) return y1;
		if(x >= x2) return y2;
		return y1 + (y2-y1)*(x-x1)/(x2-x1);
	}
	
	public static double eval(QualityFunction f, int days_late){
		days_late = Math.abs(days_late);
		double d = f.getTop() - f.getBottom();
		int dx = f.getLength() - days_late;
		double dy = (dx*d*1.0)/f.getLength();
		
		return dy + f.getBottom();
	}
	public static Date next(Date d){
		Calendar cal = Calendar.getInstance();
		cal.setTime(d);
		cal.add(Calendar.DATE, 1);
		return cal.getTime();
	}

	public static Date next(Date d, int nbDays){
		Calendar cal = Calendar.getInstance();
		cal.setTime(d);
		cal.add(Calendar.DATE, nbDays);
		return cal.getTime();
	}
	public static int distance(String s_date1, String s_date2){
		Date d1 = DateTimeUtils.convertYYYYMMDD2Date(s_date1);
		Date d2 = DateTimeUtils.convertYYYYMMDD2Date(s_date2);
		return distance(d1,d2);
	}
	public static int distance(Date d1, Date d2){
		int d = 0;
		if(d1.compareTo(d2) > 0){
			Date tmpd = d2;
			while(!tmpd.equals(d1)){
				tmpd = Utility.next(tmpd,1);
				d++;
			}
			d = -d;
		}else{
			Date tmpd = d1;
			while(!tmpd.equals(d2)){
				tmpd = Utility.next(tmpd,1);
				d++;
			}
		}
		return d;
	}
	public static String dateMonthYear(Date d){
		return d.getDate() + "-" + (d.getMonth() + 1) + "-" + d.getYear();
	}
	
	public static void main(String[] args){
		System.out.println(distance("2015-12-22","2015-12-21"));
	}
}
