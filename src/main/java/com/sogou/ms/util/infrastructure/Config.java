package com.sogou.ms.util.infrastructure;

import com.sogou.ms.util.HackUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import static com.sogou.ms.util._.*;
import static com.sogou.ms.util.infrastructure.ConfigHelper.*;

/**
 * 项目中通常都有大量的可配置项，如：各种依赖服务的地址、一些服务需要的参数等。
 * 此类的考虑是：希望能集中web项目或单模块的所有配置，相关代码对OP仍具有可读性，可供线上环境配置参考。
 * 使用方式参考：
 * http://svn.sogou-inc.com/svn/mobilesearch/trunk/zhidao/front/web/src/main/java/com/sogou/wen/base/keys/Configs.java
 * <p/>
 * bugfix(2013-9): 将读取外部文件时的Properties类改为LinkedProperties。
 * 原因：Properties类是HashTable，并不按配置文件中的顺序执行，可能造成hack等出现意外情况
 */
public final class Config {

	static final Logger logger = LoggerFactory.getLogger(Config.class);

	/* ------------------------- api ------------------------- */

	// syntax sugar
	public static Config of(String key, Object defVal) {
		return of(key, defVal == null ? null : defVal.toString(), "");
	}
	public static Config of(String key, String defVal, String comment) {
		return new Config(key, defVal, comment);
	}

	// instance
	public final String key;
	public String defVal;
	public final String comment;
	Config(String key, String defVal, String comment) {
		this.key = key;
		this.defVal = defVal;
		this.comment = trimToEmpty(comment);
		saveToCache(this);
	}

	// storage
	static final ConcurrentHashMap<String, String> configs = new ConcurrentHashMap<String, String>();
	public void set(String val) {
		configs.put(this.key, val);
	}
	public static void set(String key, String val) {
		configs.put(key, val);
	}
	public String get() {
		String val = configs.get(key);
		return val != null ? val : defVal;
	}
	public String getShardsForQdb() {
		return shards(get());
	}
	public String getShardsForMemcached() {
		return shards(get()).replaceAll("\\d+@", "");
	}

	public static List<Config> listConfigs() {
		return new ArrayList<Config>(configCache.values());
	}

	/* ------------------------- util.load ------------------------- */

	// load config file
	public static void load(Properties p) {
		for (Map.Entry<Object, Object> entry : p.entrySet()) {
			String key = String.valueOf(entry.getKey());
			String val = String.valueOf(entry.getValue());

			// bell(2013-8): 这里是这样考虑的：
			// 如果希望“以配置形式，设置内部参数”，可以写这样的配置
			// hack_wait_more_time_for_recommend_debug=com.sogou.wen.base.keys.Constants::recommendWaitTime=3000
			// ！需注意，这种方式可能导致相关类提前加载，建议仅在外部配置文件中使用，且写在文件最后
			if (key.startsWith("hack")) {
				String ret = HackUtil.hack(val);
				logger.info(f("hack: %s => %s", val, ret));
			} else {
				set(key, val);
				logger.info(f("conf: %s = %s", key, val));
			}
		}
	}
	public static void load(ServletContext ctx) {
		loadServletContext(ctx);
	}
	public static String webConfContextKey = "config.properties.file";
	public static String webConfDefault = "../conf/web.conf";

	/* ------------------------- util.misc ------------------------- */

	public static List<String> parseShards(String expression) {
		return ConfigHelper.parseShards(expression);
	}

	public static String[] parseArrayShards(String expression){
		List<String> list = parseShards(expression);
		return list.toArray(new String[list.size()]);
	}

	/**
	 * 针对我们经常使用的0@xxx风格配置，简化之。代码中可简写为shards("cache01~04.wenda.yf")
	 */
	public static String shards(String expression) {
		return generateShards(expression);
	}

	// for data dir
	public static final Config dataDir = of("data_dir", "/search/odin/data/"); //
	public static String dataFile(String filename) {
		String dir = dataDir.get();
		return dir + (dir.endsWith("/") ? "" : "/") + filename;
	}

}
