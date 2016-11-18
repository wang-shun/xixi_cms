package com.sogou.ms.util.infrastructure;

import org.apache.http.annotation.GuardedBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.sogou.ms.util._.f;

public abstract class Module {

	/* ------------------------- interface ------------------------- */

	// bell(2013-9): 此处没有定义成Module(String name)的原因是，继承时仍需要写三行代码，并没有简化
	/** 给每个Module起个名字，后门等可以根据名字找到指定的Module，并restart等 */
	public abstract String name();
	public String comment() {
		return "";
	}

	public void moduleStartup() throws Exception {
	}
	public void moduleRestart() throws Exception {
		moduleStartup();
	}
	public void moduleWarmup() throws Exception {
	}
	public void moduleShutdown() throws Exception {
	}

	/* ------------------------- api.startup ------------------------- */

	public static void batchStartup(List<Class<? extends Module>> modules) {
		for (Class<? extends Module> c : modules)
			startup(c);
	}
	public static boolean startup(Class<? extends Module> c) {
		String clazz = c.getName();
		if (!started(clazz))
			try {
				long start = System.currentTimeMillis();
				getInstance(c).moduleStartup();
				logger.info(f("startup %s succ. cost %s ms.", clazz, System.currentTimeMillis() - start));
				return true;
			} catch (Exception e) {
				started.remove(clazz);
				logger.error(f("startup %s failed.", clazz), e);
				return false;
			}
		else
			return true;
	}
	// bell(2013-3): 类初始化可能造成startup被多次调用，为避免重复初始化，使用started进行标记
	private static synchronized boolean started(String clazz) {
		if (started.containsKey(clazz)) {
			return true;
		} else {
			started.put(clazz, Boolean.TRUE);
			return false;
		}
	}
	@GuardedBy("Module.class")
	private static final Map<String, Boolean> started = new HashMap<String, Boolean>();

	/* ------------------------- api.etc ------------------------- */

	public static void batchWarmup(List<Class<? extends Module>> cs) {
		for (Class<? extends Module> c : cs)
			warmUp(c);
	}
	public static void batchShutdown(List<Class<? extends Module>> cs) {
		for (Class<? extends Module> c : cs)
			shutdown(c);
	}

	public static boolean warmUp(Class<? extends Module> c) {
		try {
			long start = System.currentTimeMillis();
			getInstance(c).moduleWarmup();
			logger.info(f("warmup %s succ. cost %s ms.", c.getName(), System.currentTimeMillis() - start));
			return true;
		} catch (Exception e) {
			logger.error(f("warmup %s failed.", c.getName()), e);
			return false;
		}
	}

	public static boolean shutdown(Class<? extends Module> c) {
		try {
			long start = System.currentTimeMillis();
			getInstance(c).moduleShutdown();
			logger.info(f("shutdown %s succ. cost %s ms.", c.getName(), System.currentTimeMillis() - start));
			return true;
		} catch (Exception e) {
			logger.error(f("shutdown %s failed.", c.getName()), e);
			return false;
		}
	}

	public static boolean restart(Class<? extends Module> c) {
		try {
			long start = System.currentTimeMillis();
			getInstance(c).moduleRestart();
			logger.info(f("restart %s succ. cost %s ms.", c.getName(), System.currentTimeMillis() - start));
			return true;
		} catch (Exception e) {
			logger.error(f("restart %s failed.", c.getName()), e);
			return false;
		}
	}

	/* ------------------------- name ------------------------- */

	{
		if (_shortnames.containsKey(this.name()))
			logger.warn("duplicate module name: " + this.name());
		_shortnames.put(this.name(), this.getClass());
	}
	public static Class<? extends Module> findByName(String name) {
		return _shortnames.get(name);
	}
	@GuardedBy("Module.class")
	public static final Map<String, Class<? extends Module>> _shortnames = new HashMap<String, Class<? extends Module>>();

	/* ------------------------- impl ------------------------- */

	static final Logger logger = LoggerFactory.getLogger(Module.class);

	// 保证每个module只初始化一次
	public static synchronized <T> T getInstance(Class<? extends T> clazz) {
		try {
			String key = clazz.getName();
			@SuppressWarnings("unchecked")
			T cache = (T) objMap.get(key);
			if (cache == null)
				objMap.put(key, cache = clazz.newInstance());
			return cache;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	@GuardedBy("Module.class")
	static final Map<String, Object> objMap = new HashMap<String, Object>();

}
