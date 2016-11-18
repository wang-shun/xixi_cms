package com.sogou.ms.util.infrastructure.web;

import javax.servlet.jsp.tagext.TagSupport;


/** 是否启用el表达式的html转义. {@link EscapeHtmlELResolver} */
// ref: https://github.com/pukkaone/webappenhance/blob/master/src/main/java/com/github/pukkaone/jsp/OutTag.java
public class KeepHtmlTag extends TagSupport {
	private static final long serialVersionUID = 1L;

	public KeepHtmlTag() {
	}

	@Override
	public int doStartTag() {
		pageContext.setAttribute(EscapeHtmlELResolver.ATTR_ENABLE_ESCAPE, Boolean.FALSE);
		return EVAL_BODY_INCLUDE;
	}

	@Override
	public int doEndTag() {
		pageContext.setAttribute(EscapeHtmlELResolver.ATTR_ENABLE_ESCAPE, null);
		return EVAL_PAGE;
	}

}
