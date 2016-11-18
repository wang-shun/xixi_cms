package web.base;

import com.sogou.ms.util.EnvUtil;
import com.sogou.ms.util._;
import com.sogou.ms.util.infrastructure.Module;
import com.sogou.ms.util.infrastructure.web.EnhancedBeanELResolver;
import com.sogou.ms.util.infrastructure.web.EscapeHtmlELResolver;
import com.sogou.ms.util.toolkit.AutoLog;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.jsp.JspApplicationContext;
import javax.servlet.jsp.JspFactory;
import java.util.List;

/**
 * Created by Jarod on 2015/10/14.
 */
public class InitListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext ctx = sce.getServletContext();
        initInfrastructure(ctx);
        initModules(ctx);
    }

    private void initInfrastructure(ServletContext ctx) {
        JspApplicationContext appContext = JspFactory.getDefaultFactory().getJspApplicationContext(ctx);
        // bell(2013-4): 必须保证先过escape、再过enhancedBean，否则enhancedBean输出的就会跳过escape检查了
        // 但此处，jetty与resin的行为是刚好相反的，jetty先注册的后过，resin后注册的先过
        // 现在先临时根据os判断一下，如果以后发现此问题比较严重，自写一个CompositeELResolver把这两个resolver合成一个
        if (EnvUtil.isWindows()) {
            appContext.addELResolver(new EnhancedBeanELResolver());
            appContext.addELResolver(new EscapeHtmlELResolver());
        } else {
            appContext.addELResolver(new EscapeHtmlELResolver());
            appContext.addELResolver(new EnhancedBeanELResolver());
        }
    }

    private static final List moduleList = _.asList(
            // list of which extend Module

    );

    private void initModules(ServletContext ctx) {
        Module.batchStartup(moduleList);
        Module.batchWarmup(moduleList);

        AutoLog.printAll();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        Module.batchShutdown(moduleList);

        AutoLog.printAll();
    }
}
