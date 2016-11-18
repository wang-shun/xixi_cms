package com.sogou.ms.util.infrastructure.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.el.ELContext;
import javax.el.ELResolver;
import java.beans.FeatureDescriptor;
import java.util.Iterator;

/** 扩充${foo.bar}的语义. @see {@link EnhancedBeanReader#get(Object, String)} */
public class EnhancedBeanELResolver extends ELResolver {

	@Override
	public Object getValue(ELContext ctx, Object base, Object name) {
		if (base == null || name == null || !(name instanceof String))
			return null;

		// bell(2013-4): <c:forEach> status
		// java.lang.IllegalAccessException: Class com.sogou.wen.web.util.BeanReader can not access a member of class javax.servlet.jsp.jstl.core.LoopTagSupport$1Status with modifiers "public"
		// 15:26:02.325 INFO  [c.s.w.w.u.BeanReader] fail get class javax.servlet.jsp.jstl.core.LoopTagSupport$1Status::first from javax.servlet.jsp.jstl.core.LoopTagSupport$1Status@570180
		// 23:31:02.482 INFO  [c.s.w.w.i._BeanReader] fail get class com.caucho.jsp.IteratorLoopSupportTag::first from com.caucho.jsp.IteratorLoopSupportTag@3356b3b4
		if (base.getClass().getName().startsWith("javax.servlet.jsp.jstl")
				|| base.getClass().getName().startsWith("com.caucho.jsp."))
			return null;

		try {
			EnhancedBeanReader meta = EnhancedBeanReader.of(base.getClass());
			Object val = meta.get(base, (String) name);
			ctx.setPropertyResolved(true);
			return val;
		} catch (Exception e) {
			logger.error("", e);
			return null;
		}
	}
	static final Logger logger = LoggerFactory.getLogger(EnhancedBeanELResolver.class);

	/* ------------------------- useless ------------------------- */

	public Class<?> getCommonPropertyType(ELContext elcontext, Object obj) {
		return null;
	}
	public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext elcontext, Object obj) {
		return null;
	}
	public Class<?> getType(ELContext elcontext, Object obj, Object obj1) {
		return null;
	}
	public boolean isReadOnly(ELContext elcontext, Object obj, Object obj1) {
		return false;
	}
	public void setValue(ELContext elcontext, Object obj, Object obj1, Object obj2) {
	}

}
