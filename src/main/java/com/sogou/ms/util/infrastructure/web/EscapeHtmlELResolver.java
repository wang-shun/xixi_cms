package com.sogou.ms.util.infrastructure.web;


import com.sogou.ms.util.EscapeUtil;

import javax.el.ELContext;
import javax.el.ELResolver;
import javax.servlet.jsp.JspContext;
import java.beans.FeatureDescriptor;
import java.util.Iterator;


/**
 * 避免xss攻击，所有的el表达式默认进行html转义。对于不需要转义的el表达式，配合{@link KeepHtmlTag}
 */
// ref: http://pukkaone.github.io/2011/01/03/jsp-cross-site-scripting-elresolver.html
// ref: https://github.com/pukkaone/webappenhance/blob/master/src/main/java/com/github/pukkaone/jsp/EscapeXmlELResolver.java
public class EscapeHtmlELResolver extends ELResolver {

	@Override
	public Object getValue(ELContext context, Object base, Object name) {
		JspContext pageContext = (JspContext) context.getContext(JspContext.class);
		if (pageContext.getAttribute(ATTR_ENABLE_ESCAPE) == Boolean.FALSE)
			return null;

		try {
			// bell(2013-4): 第二次进入时，此处为true，直接略过
			if (excludeMe.get())
				return null;

			excludeMe.set(Boolean.TRUE);
			Object value = context.getELResolver().getValue(context, base, name);
			// if (value != null)
			//	System.out.println("type > " + value.getClass() + "\t\t" + value);
			if (value instanceof String) {
				value = EscapeUtil.escapeHtml(value);
			}
			return value;
		} finally {
			excludeMe.set(Boolean.FALSE);
		}
	}

	static final String ATTR_ENABLE_ESCAPE = EscapeHtmlELResolver.class.getName() + ".enable";
	private ThreadLocal<Boolean> excludeMe = new ThreadLocal<Boolean>() {
		protected Boolean initialValue() {
			return Boolean.FALSE;
		}
	};

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
