package com.sogou.ms.util.infrastructure.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;

import static com.sogou.ms.util._.*;

public class ATag extends TagSupport {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8154793522701822912L;
	private String href = null;
	private String stop_on_path = null;
	private String stop_on_param_key = null;
	private String stop_on_param_value = null;
	private String stop_on_attr_key = null;
	private String stop_on_attr_value = null;

	public void setHref(String href) {
		this.href = href;
	}
	public void setStopOnPath(String stop_on_path) {
		this.stop_on_path = stop_on_path;
	}
	public void setStopOnParam(String param) {
		String[] parts = trimToEmpty(param).split("=", 2);
		if (parts.length == 2) {
			this.stop_on_param_key = parts[0];
			this.stop_on_param_value = parts[1];
		}
	}
	public void setStopOnAttribute(String attr){
		String[] parts = trimToEmpty(attr).split("=", 2);
		if (parts.length == 2) {
			this.stop_on_attr_key = parts[0];
			this.stop_on_attr_value = parts[1];
		}
	}

	/* ------------------------- logic ------------------------- */

	private boolean stop() {
		if (nonEmpty(stop_on_path)) {
			HttpServletRequest req = (HttpServletRequest) pageContext.getRequest();
			return req.getRequestURI().equals(stop_on_path);
		}
		if (nonEmpty(stop_on_param_key)) {
			String value = pageContext.getRequest().getParameter(stop_on_param_key);
			if (isEmpty(stop_on_param_value)) {
				return isEmpty(value);
			} else {
				return stop_on_param_value.equals(value);
			}
		}
		if (nonEmpty(stop_on_attr_key)) {
			String value = (String)pageContext.getRequest().getAttribute(stop_on_attr_key);
			if (isEmpty(stop_on_attr_value)) {
				return isEmpty(value);
			} else {
				return stop_on_attr_value.equals(value);
			}
		}
		return false;
	}

	/* ------------------------- override ------------------------- */

	@Override
	public int doStartTag() throws JspException {
		boolean stop = stop();
		pageContext.getRequest().setAttribute("a_tag_stop", stop);

		if (!stop)
			try {
				// bell(2014-4): 通常传入的时候用el表达式，el表达式自己会带一次转义，所以这个href已经是转义后的
				pageContext.getOut().write("<a href='" + href + "'>");
			} catch (IOException e) {
				throw new JspException("x:a startTag err.", e);
			}
		return EVAL_BODY_INCLUDE;
	}

	@Override
	public int doEndTag() throws JspException {
		try {
			Boolean stop = (Boolean) pageContext.getRequest().getAttribute("a_tag_stop");
			pageContext.getRequest().removeAttribute("a_tag_stop");
			if (!stop)
				pageContext.getOut().write("</a>");
		} catch (IOException e) {
			throw new JspException("x:a endTag err.", e);
		}
		return EVAL_PAGE;
	}

}
