package com.sogou.ms.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.text.translate.CharSequenceTranslator;
import org.apache.commons.lang3.text.translate.LookupTranslator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * bell(2013-8): 常用工具方法小集合，旨在弥补Java语法的不足
 * 建议import static使用，并修改IDE配置中的import合并规则为：2条import static就合并为.*
 * Eclipse："Java / Code Style / Organize Imports / Number of static imports needed for .*" = 2
 * IntelliJ："Code Style / Java / Imports / Names count to use static import with *" = 2
 * <p/>
 * <p/>
 * bell(2013-8): 后续想法：如果此类的方法进一步膨胀，可以考虑用这种方式降低维护成本：
 * _ extends _string extends _collection extends _log extends _misc
 * (这样import static仍生效，不过ide的提示会很奇怪)
 * 后续想法2：参考Java8引入Closure的细节，添加相关工具方法
 */
public final class _ {


	/* ------------------------- string ------------------------- */

	// format
	public static String f(String format, Object... args) {
		return String.format(format, args);
	}

	// with empty
	public static String trimToEmpty(String s) {
		return s == null ? "" : s.trim();
	}
	public static String trimToNull(String s) {
		String trimed = trimToEmpty(s);
		return trimed.isEmpty() ? null : trimed;
	}
	public static boolean isEmpty(String s) {
		return s == null || s.length() == 0;
	}
	public static boolean nonEmpty(String s) {
		return s != null && s.length() > 0;
	}
	@Deprecated
	public static boolean isNotEmpty(String s) {
		return s != null && s.length() > 0;
	}

	// with num
	public static int toInt(String s) {
		return NumberUtils.toInt(s, -1);
	}
	public static int toInt(String s, int def) {
		return NumberUtils.toInt(s, def);
	}
	public static long toLong(String s) {
		return NumberUtils.toLong(s, -1);
	}
	public static long toLong(String s, long def) {
		return NumberUtils.toLong(s, def);
	}
	public static double toDouble(String s) {
		return NumberUtils.toDouble(s, -1);
	}
	public static double toDouble(String s, double def) {
		return NumberUtils.toDouble(s, def);
	}
	// bell(2013-8): 默认值设为-1的考虑：确实有少数情况0不是默认值，或需要区分无效数据。使用-1作为无效值至少无害

	public static String toStr(boolean v) {
		return Boolean.toString(v);
	}
	public static String toStr(int v) {
		return Integer.toString(v);
	}
	public static String toStr(long v) {
		return Long.toString(v);
	}
	public static String toStr(double v) {
		return Double.toString(v);
	}
	public static String toStr(Object o) {
		return String.valueOf(o);
	}
	public static String toStrReflection(Object o) {
		return ToStringBuilder.reflectionToString(o);
	}

	// with regex

	/**
	 * bell(2013-11): 由于会导致intelliJ优先提示regex而不是return，且Pattern.compile()通常独占一行，认为用处不大，将于2014-1-1删除
	 */
	/*
	@Deprecated
	public static Pattern regex(String regex) {
		return Pattern.compile(regex);
	}*/
	public static String find(Pattern regex, String s) {
		return find(regex, s, 1);
	}
	public static String find(Pattern regex, String s, int group) {
		Matcher m = regex.matcher(s);
		return m.find() ? m.group(m.groupCount() >=group ? group : 0) : null;
	}
	public static int findInt(Pattern regex, String s) {
		String i = find(regex, s);
		return isEmpty(i) ? -1 : toInt(i);
	}
	public static double findDouble(Pattern regex, String s) {
		String i = find(regex, s);
		return isEmpty(i) ? -1 : toDouble(i);
	}

	// escape
	public static final String utf8 = "UTF-8";
	public static final String gbk = "GB18030";
	public static final Charset charsetUtf8 = Charset.forName(utf8);
	public static final Charset charsetGbk = Charset.forName(gbk);
	public static String urlenc(String param) {
		return urlenc(param, utf8);
	}
	public static String urlencUtf8(String param) {
		return urlenc(param, utf8);
	}
	public static String urlencGbk(String param) {
		return urlenc(param, gbk);
	}
	public static String urlenc(String param, String encoding) {
		try {
			if (param == null)
				return null;
			return URLEncoder.encode(param, encoding);
		} catch (Exception e) {
			logger.error("should not error. (encode):" + param, e);
			return param;
		}
	}
	public static String urldec(String param) {
		return urldec(param, utf8);
	}
	public static String urldecUtf8(String param) {
		return urldec(param, utf8);
	}
	public static String urldecGbk(String param) {
		return urlenc(param, gbk);
	}
	public static String urldec(String param, String encoding) {
		try {
			if (param == null)
				return null;
			return URLDecoder.decode(param, encoding);
		} catch (Exception e) {
			logger.error("should not error. (decode):" + param, e);
			return param;
		}
	}

	public static String escapeHtml(String text) {
		return _htmlTranslator.translate(text);
	}
	private static final CharSequenceTranslator _htmlTranslator = new LookupTranslator(new String[][]{ //
			{"<", "&lt;"}, // < - less-than
			{">", "&gt;"}, // > - greater-than
			{"&", "&amp;"}, // & - ampersand
			{"\"", "&#39;"}, // " - double-quote
			{"'", "&#34;"}, // XML apostrophe // bell(2013-4): ie6/7中不识别&apos;所以需要转成&#34;
	});

	// misc
	public static String join(Iterable<?> iterable, String separator) {
		return StringUtils.join(iterable, separator);
	}



	/* ------------------------- arrays ------------------------- */

	public static boolean isEmpty(long[] arr) {
		return arr == null || arr.length == 0;
	}
	public static boolean nonEmpty(long[] arr) {
		return arr != null && arr.length > 0;
	}
	@Deprecated
	public static boolean isNotEmpty(long[] arr) {
		return arr != null && arr.length > 0;
	}
	public static boolean isEmpty(byte[] arr) {
		return arr == null || arr.length == 0;
	}
	public static boolean nonEmpty(byte[] arr) {
		return arr != null && arr.length > 0;
	}
	@Deprecated
	public static boolean isNotEmpty(byte[] arr) {
		return arr != null && arr.length > 0;
	}
	public static boolean isEmpty(char[] arr) {
		return arr == null || arr.length == 0;
	}
	public static boolean nonEmpty(char[] arr) {
		return arr != null && arr.length > 0;
	}
	@Deprecated
	public static boolean isNotEmpty(char[] arr) {
		return arr != null && arr.length > 0;
	}

	/* ------------------------- collection ------------------------- */

	// builder
	public static <T> List<T> asList(T... a) {
		return Arrays.asList(a);
	}
	@SuppressWarnings("unchecked")
	public static <T> Map<String, T> asMap(Object... args) {
		if (args.length % 2 != 0)
			throw new IllegalArgumentException("args.length = " + args.length);

		Map<String, T> map = new LinkedHashMap<String, T>();
		for (int i = 0; i < args.length - 1; i += 2)
			map.put(String.valueOf(args[i]), (T) args[i + 1]);
		return map;
	}
	public static Map<String, String> asStringMap(Object... args) {
		if (args.length % 2 != 0)
			throw new IllegalArgumentException("args.length = " + args.length);

		Map<String, String> map = new LinkedHashMap<String, String>();
		for (int i = 0; i < args.length - 1; i += 2)
			map.put(String.valueOf(args[i]), args[i + 1] == null ? null : String.valueOf(args[i + 1]));
		return map;
	}

	// with empty
	public static int size(List<?> list) {
		return list != null ? list.size() : 0;
	}
	public static boolean isEmpty(Collection<?> coll) {
		return coll == null || coll.isEmpty();
	}
	public static boolean nonEmpty(Collection<?> coll) {
		return coll != null && !coll.isEmpty();
	}
	@Deprecated
	public static boolean isNotEmpty(Collection<?> coll) {
		return coll != null && !coll.isEmpty();
	}
	public static boolean isEmpty(Map<?, ?> map) {
		return map == null || map.isEmpty();
	}
	public static boolean nonEmpty(Map<?, ?> map) {
		return map != null && !map.isEmpty();
	}
	@Deprecated
	public static boolean isNotEmpty(Map<?, ?> map) {
		return map != null && !map.isEmpty();
	}
	@SuppressWarnings("unchecked")
	public static <T> List<T> emptyList() {
		return (List<T>) Collections.EMPTY_LIST;
	}
	@SuppressWarnings("unchecked")
	public static <K, V> Map<K, V> emptyMap() {
		return (Map<K, V>) Collections.EMPTY_MAP;
	}


	/* ------------------------- time ------------------------- */
	public static String tm() {
		return _.toStr(System.currentTimeMillis());
	}
	public static String tm(String timeFormat) {
		return tm(timeFormat, System.currentTimeMillis());
	}
	public static String tm(String timeFormat, long time) {
		SimpleDateFormat sdf = new SimpleDateFormat(timeFormat);
		return sdf.format(new Date(time));
	}


	/* ------------------------- log ------------------------- */

	public static void p(String s) {
		logger.info(s);
	}
	public static void p(String format, Object... args) {
		logger.info(String.format(format, args));
	}

	// log for scripts or debug.
	public static void p(Object o) {
		if (o == null) {
			logger.info("(null)");
		} else if (o instanceof Map<?, ?>) {
			Map<?, ?> map = (Map<?, ?>) o;
			p(f("map size:%s", map.size()));
			for (Map.Entry<?, ?> entry : map.entrySet())
				p(f("+ %s\t -> %s", entry.getKey(), entry.getValue()));
		} else if (o instanceof List) {
			List<?> list = (List<?>) o;
			p(f("list size:%s", list.size()));
			for (Object item : list)
				p(f("+ %s", item));
		} else if (o.getClass().isArray()) {
			if (o instanceof boolean[]) {
				p(Arrays.toString((boolean[]) o));
			} else if (o instanceof byte[]) {
				p(Arrays.toString((byte[]) o));
			} else if (o instanceof char[]) {
				p(Arrays.toString((char[]) o));
			} else if (o instanceof int[]) {
				p(Arrays.toString((int[]) o));
			} else if (o instanceof long[]) {
				p(Arrays.toString((long[]) o));
			} else if (o instanceof float[]) {
				p(Arrays.toString((float[]) o));
			} else if (o instanceof double[]) {
				p(Arrays.toString((double[]) o));
			} else {
				p(Arrays.toString((Object[]) o));
			}
		} else {
			p(String.valueOf(o));
		}
	}
	public static void pHr() {
		p("------------------------------------------------------------");
	}
	static final Logger logger = LoggerFactory.getLogger("_");



	/* ------------------------- math ------------------------- */

	public static int max(int a, int b) {
		return Math.max(a, b);
	}
	public static int min(int a, int b) {
		return Math.min(a, b);
	}



	/* ------------------------- misc ------------------------- */

	public static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
		}
	}
	public static double random() {
		return Math.random();
	}
	public static int randInt(int n) {
		return rand.nextInt(n);
	}
	static final Random rand = new Random();


}
