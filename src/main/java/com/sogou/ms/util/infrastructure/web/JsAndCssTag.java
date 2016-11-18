package com.sogou.ms.util.infrastructure.web;

import com.sogou.ms.util.Md5Util;
import com.sogou.ms.util.ResourceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import static com.sogou.ms.util._.*;

public class JsAndCssTag extends TagSupport {
	private static final long serialVersionUID = 1L;

	/* ------------------------- fields ------------------------- */

	private String src;
	public String getSrc() {
		return this.src;
	}
	public void setSrc(String src) {
		this.src = src;
	}

	private String ctx;
	public String getCtx() {
		return ctx;
	}
	public void setCtx(String ctx) {
		this.ctx = ctx;
	}

	/* ------------------------- interface ------------------------- */

	public int doStartTag() {
		try {
			String html = this.src.endsWith(".js") ? getJsTag() : //
					this.src.endsWith(".css") ? getCssTag() : "";
			this.pageContext.getOut().print(html);
		} catch (IOException e) {
		}
		return 0;
	}
	private String getJsTag() {
		String template = "<script type='text/javascript' src='%s%s' charset='utf-8'></script>";
		return f(template, getCtxPath(), getRealUrl());
	}
	private String getCssTag() {
		String template = "<link href='%s%s' rel='stylesheet' type='text/css'>";
		return f(template, getCtxPath(), getRealUrl());
	}
	private String getCtxPath() {
		return nonEmpty(ctx) ? ctx : this.pageContext.getServletContext().getContextPath();
	}

	/* ------------------------- impl ------------------------- */

	private String getRealUrl() {
		String result = _cache.get(src);
		if (result == null) {
			result = getRealUrlWithoutCache();
			_cache.put(src, result);
		}
		return result;
	}
	private static final ConcurrentHashMap<String, String> _cache = new ConcurrentHashMap<>();

	private String getRealUrlWithoutCache() {
		String path = this.pageContext.getServletContext().getRealPath("/");
		File file = new File(path + src);
		if (file.exists()) {
			return src + "?v=" + calcPostfix(file);
		} else {
			logger.error(src + " not exist!!!");
			return src;
		}
	}
	static final Logger logger = LoggerFactory.getLogger(JsAndCssTag.class);

	private static String calcPostfix(File file) {
		byte[] content = ResourceUtil.fileAsBytes(file.getAbsolutePath());
		return Md5Util.md5AsLowerHex(content).substring(0, 8);
	}

	/* ------------------------- init ------------------------- */

	public static void initJsAndCssCache(ServletContext ctx, String path) {
		File root = new File(ctx.getRealPath(path));
		if (root.isDirectory()) {
			cacheAllJsAndCss(path, root);
		}
	}
	private static void cacheAllJsAndCss(String prePath, File dir) {
		File files[] = dir.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				cacheAllJsAndCss(prePath + "/" + file.getName(), file);
			} else {
				if (file.getName().endsWith(".js") || file.getName().endsWith(".css")) {
					String path = prePath + "/" + file.getName();
					String url = path + "?v=" + calcPostfix(file);
					_cache.put(path, url);
					logger.info("tag cached: " + url);
				}
			}
		}
	}

}
