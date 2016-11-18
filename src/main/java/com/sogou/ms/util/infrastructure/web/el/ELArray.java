package com.sogou.ms.util.infrastructure.web.el;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * EL不支持vararg，也不支持overload
 * 为实现类似效果，使get操作具有append行为
 * ref: http://stackoverflow.com/questions/15497748/passing-variable-arguments-to-custom-el-function-possible
 */
public class ELArray extends AbstractMap<Object, Object> {

	private List<Object> list = new ArrayList<>();

	@Override
	public Object get(Object item) {
		list.add(item);
		return this;
	}

	@Override
	public Set<Entry<Object, Object>> entrySet() {
		throw new UnsupportedOperationException();
	}

	public static ELArray create() {
		return new ELArray();
	}

	public Object[] arr() {
		return list.toArray();
	}

}
