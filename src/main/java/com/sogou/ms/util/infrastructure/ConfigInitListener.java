package com.sogou.ms.util.infrastructure;

import com.sogou.ms.util.toolkit.AutoLog;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;


public class ConfigInitListener implements ServletContextListener {
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		ServletContext ctx = sce.getServletContext();
		Config.load(ctx);
	}
	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		AutoLog.shutdown();
	}
}
