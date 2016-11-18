package com.sogou.ms.util.infrastructure.web;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTagSupport;

import static com.sogou.ms.util._.isEmpty;
import static com.sogou.ms.util.infrastructure.web.BlockTag.*;

// ref: https://github.com/lazyman/rapid-framework/tree/master/rapid-core/src/main/java/cn/org/rapid_framework/web/tags
public class BlockContentTag extends BodyTagSupport {
	private static final long serialVersionUID = 7221813937571225908L;

	private String name;
	public void setName(String name) {
		this.name = name;
	}
	private boolean append = false;
	public void setAppend(boolean append) {
		this.append = append;
	}

	@Override
	public int doStartTag() throws JspException {
		return EVAL_BODY_BUFFERED;
	}
	@Override
	public int doEndTag() throws JspException {
		String content = getContent(getBodyContent());
		if (append) {
			String oldContent = getBlockContent(pageContext, name);
			setBlockContent(pageContext, name, isEmpty(oldContent) ? content : (oldContent + "\n" + content));
		} else {
			setBlockContent(pageContext, name, content);
		}
		return EVAL_PAGE;
	}

	/* ------------------------- util ------------------------- */

	private static String getContent(BodyContent content) {
		return content.getString().trim();
	}


}
