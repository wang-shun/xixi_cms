package com.sogou.ms.util.infrastructure;

import org.slf4j.Logger;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.sogou.ms.util._.*;

class ConfigHelper {

	static final Logger logger = Config.logger;

	/* ------------------------- api ------------------------- */

	public static void saveToCache(Config config) {
		String key = config.key;
		Config old = configCache.put(key, config);
		if (old != null)
			logger.error("duplicate config: " + key);
	}
	static final Map<String, Config> configCache = new HashMap<String, Config>();

	/* ------------------------- load config ------------------------- */

	/** Properties不保证顺序。config可能有hack，不按顺序hack会很危险。使用此类保证顺序 */
	public static final class LinkedProperties extends Properties {
		private static final long serialVersionUID = 1L;
		Map<Object, Object> linkedMap = new LinkedHashMap<Object, Object>();

		@Override
		public synchronized Object put(Object key, Object value) {
			return linkedMap.put(key, value);
		}
		@Override
		public Set<Map.Entry<Object, Object>> entrySet() {
			return linkedMap.entrySet();
		}
	}

	public static void loadServletContext(ServletContext ctx) {
		String filepath = ctx.getInitParameter(Config.webConfContextKey);
		if (filepath == null)
			filepath = Config.webConfDefault;
		if (isEmpty(filepath)) {
			logger.warn("empty config");
			return;
		}
		File file = new File(ctx.getRealPath("/"), filepath);
		if (!file.exists()) {
			logger.error("config not found: " + file.getAbsolutePath());
			return;
		}
		if (!file.canRead()) {
			logger.error("config not readable: " + file.getAbsolutePath());
			return;
		}
		try {
			Properties p = new ConfigHelper.LinkedProperties();
			p.load(new FileInputStream(file));
			Config.load(p);
		} catch (IOException e) {
			logger.error("read config err: " + e);
		}
	}

	/* ------------------------- utils ------------------------- */

	public static List<String> parseShards(String expression) {
		List<String> shards = new ArrayList<String>();
		addShards(shards, expression);
		return shards;
	}

	public static String generateShards(String expression) {
		// 如果传入的就是n@xxx格式，则不作任何处理
		if (expression.contains("@"))
			return expression;

		List<String> shards = parseShards(expression);
		StringBuilder out = new StringBuilder();
		for (int i = 0; i < shards.size(); i++)
			out.append(f(" %d@%s", i, shards.get(i)));
		return out.toString().trim();
	}
	private static void addShards(List<String> shards, String expression) {
		expression = trimToEmpty(expression);
		if (isEmpty(expression))
			return;
		if (expression.contains(",")) {
			for (String token : expression.split(","))
				addShards(shards, token);
			return;
		}
		if (expression.contains("~")) {
			Matcher m = Pattern.compile("^(\\w+?)(\\d+)~(\\d+)(.*)$").matcher(expression);
			if (!m.matches())
				return;
			int start = toInt(m.group(2));
			int end = toInt(m.group(3));
			if (start <= end)
				for (int i = start; i <= end; i++) {
					String host = m.group(1) + f("%0" + m.group(2).length() + "d", i) + m.group(4);
					addShards(shards, host);
				}
			else
				for (int i = start; i >= end; i--) {
					String host = m.group(1) + f("%0" + m.group(2).length() + "d", i) + m.group(4);
					addShards(shards, host);
				}
			return;
		}
		shards.add(expression);
	}

}
