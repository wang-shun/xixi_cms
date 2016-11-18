package com.sogou.ms.util;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public final class TimeUtil {

	public static final long second = 1000;
	public static final long minute = 60 * second;
	public static final long hour = 60 * minute;
	public static final long day = 24 * hour;

	static final DateTimeFormatter compactDateFormatter = DateTimeFormat.forPattern("yyyyMMdd");
	static final DateTimeFormatter compactTimeFormatter = DateTimeFormat.forPattern("yyyyMMddHHmmss");
	static final DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd");
	static final DateTimeFormatter timeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");


	public static long parseDate(String text) {
		return dateFormatter.parseMillis(text);
	}
	public static long parseCompactDate(String text) {
		return compactDateFormatter.parseMillis(text);
	}
	public static long parseTime(String text) {
		return timeFormatter.parseMillis(text);
	}
	public static long parseCompactTime(String text) {
		return compactTimeFormatter.parseMillis(text);
	}

	public static String formatDate(long millis) {
		return dateFormatter.print(millis);
	}
	public static String formatCompactDate(long millis) {
		return compactDateFormatter.print(millis);
	}
	public static String formatTime(long millis) {
		return timeFormatter.print(millis);
	}
	public static String formatCompactTime(long millis) {
		return compactTimeFormatter.print(millis);
	}

}
