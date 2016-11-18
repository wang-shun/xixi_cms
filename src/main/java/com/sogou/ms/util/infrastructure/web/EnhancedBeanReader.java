package com.sogou.ms.util.infrastructure.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.sogou.ms.util._.*;

class EnhancedBeanReader {

	public Object get(Object bean, String name) throws Exception {
		if (bean == null)
			return null;

		try {
			// *1. str.*()
			if (name.startsWith("magic_")) {
				return magic(bean.toString(), name);
			}

			// *2. call foo(bar)
			if (name.contains("__")) {
				String[] parts = name.split("__", 2);
				return getWithParam(bean, parts[0], parts[1]);
			}

			// 1. call foo()
			Method method1 = method(name);
			if (method1 != null)
				return method1.invoke(bean);

			// 2. call getFoo()
			Method method2 = method("get" + name);
			if (method2 != null)
				return method2.invoke(bean);

			// 3. public foo;
			Field field = field(name);
			if (field != null)
				return field.get(bean);

			// 4. map.get("foo")
			if (bean instanceof Map<?, ?>)
				return ((Map<?, ?>) bean).get(name);

			logger.info(f("fail get %s::%s from %s", bean.getClass(), name, bean));
		} catch (Exception e) {
			if (e.getClass().getName().equals("java.lang.reflect.InvocationTargetException")
					&& e.getCause() instanceof Exception)
				e = (Exception) e.getCause();
			logger.info(f("err get %s::%s (%s) bean(%s)", bean.getClass(), name, e.toString(), bean));
		}

		return null;
	}

	private static String magic(String s, String name) {
		if (name.equals("magic_encodeUrl"))
			return urlencUtf8(s);
		return "";
	}

	private Object getWithParam(Object bean, String name, String param) throws Exception {
		Method method = method1p(name);
		Class<?> p1type = method.getParameterTypes()[0];
		Object p1 = trans(param, p1type);
		return method.invoke(bean, p1);
	}
	private static Object trans(String val, Class<?> type) {
		if (type.isInstance(val))
			return val;
		if (int.class.equals(type))
			return toInt(val);
		if (long.class.equals(type))
			return toLong(val);
		// TODO: may be other type casts
		if (val != null)
			logger.error("TODO-TYPECAST: " + val.getClass() + " -> " + type);
		return null;
	}

	/* ------------------------- cache ------------------------- */

	public static EnhancedBeanReader of(Class<?> type) {
		EnhancedBeanReader cache = _metaCache.get(type.getName());
		if (cache == null)
			_metaCache.put(type.getName(), cache = new EnhancedBeanReader(type));
		return cache;
	}
	static final Map<String, EnhancedBeanReader> _metaCache = new ConcurrentHashMap<String, EnhancedBeanReader>();
	static final Logger logger = LoggerFactory.getLogger(EnhancedBeanReader.class);
	// public static final Object NULL = new Object();

	/* ------------------------- impl ------------------------- */

	final Map<String, Field> fields = new HashMap<String, Field>();
	final Map<String, Method> methods = new HashMap<String, Method>();
	final Map<String, Method> methodsWith1Param = new HashMap<String, Method>();
	public EnhancedBeanReader(Class<?> type) {
		for (Field field : type.getFields()) {
			int mod = field.getModifiers();
			if (!Modifier.isStatic(mod) && Modifier.isPublic(mod) /*&& !Modifier.isFinal(mod)*/) {
				// field.setAccessible(true);
				fields.put(field.getName().toLowerCase(), field);
			}
		}
		for (Method method : type.getMethods()) {
			int mod = method.getModifiers();
			if (!Modifier.isStatic(mod) && Modifier.isPublic(mod) //
					&& !void.class.equals(method.getReturnType())) {

				if (method.getParameterTypes().length == 0)
					methods.put(method.getName().toLowerCase(), method);
				else if (method.getParameterTypes().length == 1)
					methodsWith1Param.put(method.getName().toLowerCase(), method);

			}
		}
	}

	Field field(String name) {
		return fields.get(name.toLowerCase());
	}
	Method method(String name) {
		return methods.get(name.toLowerCase());
	}
	Method method1p(String name) {
		return methodsWith1Param.get(name.toLowerCase());
	}

	/* ------------------------- debug ------------------------- */

	public static void main(String[] args) throws Exception {
//		test(UserStat.byId(1047), "uiNewMsgCount");
//		test(UserStat.byId(1047), "uiTopNewMsgList");
	}
	private static void test(Object obj, String key) throws Exception {
		EnhancedBeanReader meta = new EnhancedBeanReader(obj.getClass());
		Object val = meta.get(obj, key);
		p(f("%s = %s", key, val));
	}

}
