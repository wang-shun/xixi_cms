package com.sogou.ms.util;

import java.lang.reflect.Field;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.sogou.ms.util._.*;

/** 主要用于基于反射的通用后门 */
public class HackUtil {

	public static String hack(String cmd) {
		if (nonEmpty(cmd)) {
			Matcher m;
			for (Pattern pattern : assignPatterns)
				if ((m = pattern.matcher(cmd)).matches())
					return setProperty(m.group(1), m.group(2), m.group(3));
			for (Pattern pattern : retrievePatterns)
				if ((m = pattern.matcher(cmd)).matches())
					return getProperty(m.group(1), m.group(2));
		}
		return "invalid expression";
	}
	static final Pattern[] assignPatterns = {
			Pattern.compile("^([\\w.]+?)\\s*::\\s*([\\w]+?)\\s*=\\s*(.*)"),
			Pattern.compile("^([\\w.]+?)\\s*.\\s*([\\w]+?)\\s*=\\s*(.*)")
	};
	static final Pattern[] retrievePatterns = {
			Pattern.compile("^([\\w.]+?)\\s*::\\s*([\\w]+?)$"),
			Pattern.compile("^([\\w.]+?)\\s*.\\s*([\\w]+?)$")
	};

	public static String getProperty(String className, String key) {
		try {
			Field field = getField(className, key);
			String val = fieldGet(field);
			return f("%s::%s = %s", className, key, val);
		} catch (Exception e) {
			return "set field err: " + e;
		}
	}
	public static String setProperty(String className, String key, String val) {
		try {
			Field field = getField(className, key);
			String from = fieldGet(field);
			fieldSet(field, val);
			String to = fieldGet(field);
			return f("set %s::%s from %s to %s", className, key, from, to);
		} catch (Exception e) {
			return "set field err: " + e;
		}
	}

	/* ------------------------- util ------------------------- */

	private static Field getField(String className, String key) throws Exception {
		final Class<?> clazz = Class.forName(className);
		final Field field = clazz.getDeclaredField(key);
		field.setAccessible(true);
		return field;
	}

	private static String fieldGet(Field field) throws IllegalAccessException {
		Class<?> type = field.getType();
		if (type.isAssignableFrom(boolean.class)) {
			return field.getBoolean(null) ? "on" : "off";
		} else if (type.isAssignableFrom(int.class)) {
			return toStr(field.getInt(null));
		} else if (type.isAssignableFrom(long.class)) {
			return toStr(field.getLong(null));
		} else if (type.isAssignableFrom(float.class)) {
			return toStr(field.getLong(null));
		} else if (type.isAssignableFrom(double.class)) {
			return toStr(field.getDouble(null));
		} else {
			return String.valueOf(field.get(null));
		}
	}
	private static void fieldSet(Field field, String val) throws IllegalAccessException {
		Class<?> type = field.getType();
		if (type.isAssignableFrom(boolean.class)) {
			val = trimToEmpty(val).toLowerCase();
			boolean v = val.equals("on") || val.equals("true") || val.equals("ture") || val.equals("yes")
					|| val.equals("1");
			field.setBoolean(null, v);
		} else if (type.isAssignableFrom(int.class)) {
			field.setInt(null, Integer.parseInt(trimToEmpty(val)));
		} else if (type.isAssignableFrom(long.class)) {
			field.setLong(null, Long.parseLong(trimToEmpty(val)));
		} else if (type.isAssignableFrom(float.class)) {
			field.setFloat(null, Float.parseFloat(trimToEmpty(val)));
		} else if (type.isAssignableFrom(double.class)) {
			field.setDouble(null, Double.parseDouble(trimToEmpty(val)));
		} else if (type.isAssignableFrom(String.class)) {
			field.set(null, val);
		} else {
			throw new IllegalAccessException("unsupported type: " + type);
		}
	}

}
