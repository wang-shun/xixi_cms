package web.util;

import com.sogou.ms.util._;

import javax.servlet.http.HttpServletRequest;

/**
 * User: madtracy
 * Time: 2016/5/25.
 */
public class ReqUtil {

	public static String get(HttpServletRequest req, String key){
		return _.trimToEmpty(req.getParameter(key));
	}

	public static String get(HttpServletRequest req, String key, String defVal){
		String value = req.getParameter(key);
		return _.isEmpty(value) ? defVal : value;
	}

	public static int getInt(HttpServletRequest req, String key, int defVal){
		String value = req.getParameter(key);
		return _.toInt(value,defVal);
	}
	
	public static long getLong(HttpServletRequest req, String key, long defVal){
		String value = req.getParameter(key);
		return _.toLong(value,defVal);
	}
	
	public static String getUuid(HttpServletRequest req) {
		// zhjj(2014-3-11):方便各个地方使用，nginx模块中的cookie，每个请求uuid唯一
		return trimToEmpty(CookieUtil.getCookie(req, "uuid"));
	}
	
	public static String getIp(HttpServletRequest req) {
		// bell(2013-2): 代理转发前ip识别由apache/nginx实现，取到的即为真实ip，无需考虑代理中转问题
		return req.getRemoteAddr();
	}

	public static String getUA(HttpServletRequest req) {
		String ua = req.getHeader("User-Agent");
		return _.trimToEmpty(ua);
	}
	public static String getAccept(HttpServletRequest req) {
		return req.getHeader("Accept");
	}


	public static String trimToEmpty(String s) {
		return s == null ? "" : s.trim();
	}

	public static boolean checkWeixin(HttpServletRequest req){
		String ua = getUA(req);
		return _.isEmpty(ua)?false:ua.contains("MicroMessenger");
	}
}
