package com.sogou.ms.util.toolkit;

import com.google.common.base.Function;
import com.sun.org.apache.xpath.internal.operations.Bool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import static com.sogou.ms.util._.f;

/**
 * 仅供DBRunner使用，请勿外部使用
 */
public class ResultSetMapper<T> implements Function<Map<String, Object>, T> {

	public T from(ResultSet rs) {
		T bean = null;
		try {
			bean = clazz.newInstance();

			ResultSetMetaData meta = rs.getMetaData();
			int cols = meta.getColumnCount();
			for (int col = 1; col <= cols; col++) {
				String key = meta.getColumnLabel(col);
				fill(bean, key, rs.getObject(col));
			}
		} catch (Exception e) {
			logger.error(f("extract fail for %s", clazz), e);
		}
		return bean;
	}
	public T apply(Map<String, Object> row) {
		T bean = null;
		try {
			bean = clazz.newInstance();
			for (Entry<String, Object> entry : row.entrySet()) {
				fill(bean, entry.getKey(), entry.getValue());
			}
		} catch (Exception e) {
			logger.error(f("extract fail for %s", clazz), e);
		}
		return bean;
	}

	@SuppressWarnings("unchecked")
	private void fill(T bean, String name, Object val) throws Exception {
		name = name.toLowerCase();

		// 1. call setFoo(X)
		Method method1 = method("set" + name);
		if (method1 != null) {
			fillMethod(method1, bean, val);
			return;
		}

		// 2. call foo(X)
		Method method2 = method(name);
		if (method2 != null) {
			fillMethod(method2, bean, val);
			return;
		}

		// 3. public foo;
		Field field1 = field(name);
		if (field1 != null) {
			fillField(field1, bean, val);
			return;
		}

		// 4. public _foo;
		Field field2 = field("_" + name);
		if (field2 != null) {
			fillField(field2, bean, val);
			return;
		}

		// 4. map.put("foo", X)
		if (bean instanceof Map<?, ?>) {
			((Map<String, Object>) bean).put(name, val);
			return;
		}

		logger.info(f("miss entry %s :: %s", clazz, name));
	}

	static void fillMethod(Method method, Object bean, Object sqlVal) throws Exception {
		Object val = trans(sqlVal, method.getParameterTypes()[0]);
		if (val != null)
			method.invoke(bean, val);
	}
	static void fillField(Field field, Object bean, Object sqlVal) throws Exception {
		Object val = trans(sqlVal, field.getType());
		if (val != null)
			field.set(bean, val);
	}

	static Object trans(Object sqlVal, Class<?> type) {
		if (type.isInstance(sqlVal))
			return sqlVal;
		if (int.class.equals(type)) {
			if (sqlVal instanceof Number)
				return ((Number) sqlVal).intValue();
		} else if (long.class.equals(type)) {
			if (sqlVal instanceof Number)
				return ((Number) sqlVal).longValue();
			if (sqlVal instanceof Date)
				return ((Date) sqlVal).getTime();
		} else if (boolean.class.equals(type)) {
			if (sqlVal instanceof Boolean)
				return (Boolean) sqlVal;
			if (sqlVal instanceof Number)
				return ((Number) sqlVal).intValue() != 0;
		}

		// TODO: may be other type casts
		if (sqlVal != null)
			logger.error("TYPECAST: " + sqlVal.getClass() + " -> " + type);
		return null;
	}

	/* ------------------------- cache ------------------------- */

	public static <T> ResultSetMapper<T> of(Class<T> type) {
		@SuppressWarnings("unchecked")
		ResultSetMapper<T> cache = (ResultSetMapper<T>) _metaCache.get(type.getName());
		if (cache == null)
			_metaCache.put(type.getName(), cache = new ResultSetMapper<T>(type));
		return cache;
	}
	static final Map<String, ResultSetMapper<?>> _metaCache = new ConcurrentHashMap<String, ResultSetMapper<?>>();
	static final Logger logger = LoggerFactory.getLogger(ResultSetMapper.class);

	/* ------------------------- impl ------------------------- */

	final Class<T> clazz;
	final Map<String, Field> fields = new HashMap<String, Field>();
	final Map<String, Method> methods = new HashMap<String, Method>();

	public ResultSetMapper(Class<T> type) {
		this.clazz = type;
		for (Field field : type.getFields()) {
			int mod = field.getModifiers();
			if (!Modifier.isStatic(mod) && Modifier.isPublic(mod))
				fields.put(field.getName().toLowerCase(), field);
		}
		for (Method method : type.getMethods()) {
			int mod = method.getModifiers();
			if (!Modifier.isStatic(mod) && Modifier.isPublic(mod) //
					&& method.getParameterTypes().length == 1)
				methods.put(method.getName().toLowerCase(), method);
		}
	}

	Field field(String name) {
		return fields.get(name);
	}
	Method method(String name) {
		return methods.get(name);
	}

}
