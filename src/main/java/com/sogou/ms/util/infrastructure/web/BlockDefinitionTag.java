package com.sogou.ms.util.infrastructure.web;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;

// ref: https://github.com/lazyman/rapid-framework/tree/master/rapid-core/src/main/java/cn/org/rapid_framework/web/tags
public class BlockDefinitionTag extends TagSupport {
	private static final long serialVersionUID = -262538935082625461L;

	private String name;
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public int doStartTag() throws JspException {
		return hasBlockContent(pageContext, name) ? SKIP_BODY : EVAL_BODY_INCLUDE;
	}

	@Override
	public int doEndTag() throws JspException {
		String content = getBlockContent(pageContext, name);
		if (content != null)
			try {
				pageContext.getOut().write(content);
			} catch (IOException e) {
				throw new JspException("x:block endTag err. block name:" + name, e);
			}
		return EVAL_PAGE;
	}

	/* ------------------------- util ------------------------- */

	static boolean hasBlockContent(PageContext pageCtx, String name) {
		return getBlockContent(pageCtx, name) != null;
	}
	static String getBlockContent(PageContext pageCtx, String name) {
		return (String) pageCtx.getRequest().getAttribute(getBlockKey(name));
	}
	static void setBlockContent(PageContext pageCtx, String name, String value) {
		pageCtx.getRequest().setAttribute(getBlockKey(name), value);
	}
	private static String getBlockKey(String name) {
		return "__x:block__" + name;
	}

}
