package com.sogou.ms.util.infrastructure;

import com.sogou.ms.util.HackUtil;
import com.sogou.ms.util.ServletUtil;
import com.sogou.ms.util.toolkit.MyExecutor;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.sogou.ms.util._.*;

// bell(2014-8): 两种用法：
// 1. 作为servlet配到项目里，实现doService来补充额外的后门
// 2. 直接调用process()方法，如返回false，再继续处理额外后门
public abstract class BaseOpServlet extends HttpServlet {

	/* ------------------------- as util ------------------------- */
	// bell(2014-8): 这里写的太烂了，以后想想怎么梳理下结构

	// 已处理返回true
	public static boolean process(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		boolean blocked = !ServletUtil.isIntranet(req);
		if (blocked) {
			resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
			LoggerFactory.getLogger("_").info("ip {} blocked.", req.getRemoteAddr());
			return false;
		}

		String body = processOp(req);
		if (body != null) {
			resp.getWriter().println(body);
			return true;
		}
		return false;
	}

	/* ------------------------- as servlet ------------------------- */

	@Override
	final protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		boolean blocked = !ServletUtil.isIntranet(req);
		if (blocked) {
			resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
			LoggerFactory.getLogger("_").info("ip {} blocked.", req.getRemoteAddr());
			return;
		}

		String body = _doService(req);
		resp.getWriter().println(body);
	}
	private final String _doService(HttpServletRequest req) {
		String body = processOp(req);
		if (body != null)
			return body;
		return doService(req);
	}

	protected abstract String doService(HttpServletRequest req);

	/* ------------------------- logic ------------------------- */

	private static String processOp(HttpServletRequest req) {
		// bell(2014-8): 此处不能使用getRequestUri。当部署到/xxx/路径下时，需从/xxx/之后开始判断
		switch (req.getServletPath()) {
			// config
			case "/op/config": {
				StringBuilder out = new StringBuilder();
				List<Config> configs = Config.listConfigs();
				Collections.sort(configs, new Comparator<Config>() {
					public int compare(Config o1, Config o2) {
						return o1.key.compareTo(o2.key);
					}
				});
				for (Config config : configs) {
					String comment = isNotEmpty(config.comment) ? " # " + config.comment : "";
					out.append(f("%s = %s%s\n", config.key, config.get(), comment));
				}
				return out.toString();
			}
			case "/op/config/set": { // ? cfg_key = val
				String[] query = trimToEmpty(req.getQueryString()).split("=", 2);
				if (query.length == 2) {
					Config config = ConfigHelper.configCache.get(query[0]);
					if (config != null) {
						String oldVal = config.get();
						config.set(query[1]);
						return f("set %s from (%s) to (%s)", query[0], oldVal, query[1]);
					} else {
						return "no such config: " + query[0];
					}
				}
				return "invalid expression";
			}

			// module
			case "/op/module": {
				StringBuilder out = new StringBuilder();
				List<Class<? extends Module>> values = new ArrayList<Class<? extends Module>>(Module._shortnames.values());
				Collections.sort(values, new Comparator<Class<? extends Module>>() {
					public int compare(Class<? extends Module> o1, Class<? extends Module> o2) {
						Module m1 = Module.getInstance(o1);
						Module m2 = Module.getInstance(o2);
						return m1.name().compareTo(m2.name());
					}
				});
				for (Class<? extends Module> clazz : values) {
					Module module = Module.getInstance(clazz);
					String comment = isNotEmpty(module.comment()) ? " # " + module.comment() : "";
					out.append(f("%s(%s)%s\n", module.name(), clazz.getName(), comment));
				}
				return out.toString();
			}
			case "/op/module/restart": { // ? mod_name
				String name = req.getQueryString();
				Class<? extends Module> clazz = Module.findByName(name);
				if (clazz == null)
					return "module not found: " + name;
				Module module = Module.getInstance(clazz);
				try {
					long start = System.currentTimeMillis();
					module.moduleRestart();
					return f("module %s restarted.\ncost: %s ms", name, System.currentTimeMillis() - start);
				} catch (Exception e) {
					StringWriter err = new StringWriter();
					e.printStackTrace(new PrintWriter(err));
					return f("module %s restart failed.\nerr: %s", name, err.toString());
				}
			}

			// exec
			case "/op/exec": {
				MyExecutor.Stat stat = MyExecutor.instance().getStat();
				return "[running]:\n" + stat.printRunning() + "\n\n[finished]:\n" + stat.printFinished() + "\n\n[cost]:\n" + stat.printFinishedCosts() + "\n\n[avg]:\n" + stat.printAverageCosts();
			}
			case "/op/exec/clear": {
				MyExecutor.instance().resetStat();
				return "stat cleared";
			}
			case "/op/exec/restart": {
				MyExecutor.restart();
				return "exec restarted";
			}

			// hack
			case "/op/hack": {
				return HackUtil.hack(req.getQueryString());
			}

			default:
				return null;
		}
	}

}
