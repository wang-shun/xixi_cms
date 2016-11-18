package com.sogou.ms.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.Enumeration;
import java.util.regex.Pattern;

import static com.sogou.ms.util._.*;

public class ServletUtil {

    static final Logger logger = LoggerFactory.getLogger(ServletUtil.class);

	/* ------------------------- req.properties ------------------------- */

    public static boolean isPost(HttpServletRequest req) {
        return "post".equalsIgnoreCase(req.getMethod());
    }

    public static boolean isAjax(HttpServletRequest req) {
        // bell(2011-9): 目前js库基本都会加这个属性，这么判断还是靠谱的
        return "XMLHttpRequest".equalsIgnoreCase(req.getHeader("X-Requested-With"));
    }

    public static String getUuid(HttpServletRequest req) {
        // zhjj(2014-3-11):方便各个地方使用，nginx模块中的cookie，每个请求uuid唯一
        return _.trimToEmpty(getCookie(req, "uuid"));
    }

    public static String getIp(HttpServletRequest req) {
        // bell(2013-2): 代理转发前ip识别由apache/nginx实现，取到的即为真实ip，无需考虑代理中转问题
        return req.getRemoteAddr();
    }

    public static String mockIp() {
        return f("%s.%s.%s.%s", (int) (random() * 0x100), (int) (random() * 0x100),
                (int) (random() * 0x100), (int) (random() * 0x100));
    }

    public static boolean isIntranet(HttpServletRequest req) {
        return isIntranet(getIp(req));
    }

    public static boolean isIntranet(String ip) {
        return intranetIpPattern.matcher(ip).find();
    }

    // ref: http://en.wikipedia.org/wiki/IP_address#IPv4_private_addresses
    static final Pattern intranetIpPattern = Pattern.compile(
            "(^127\\.)|(^10\\.)|(^172\\.1[6-9]\\.)|(^172\\.2[0-9]\\.)|(^172\\.3[0-1]\\.)|(^192\\.168\\.)|(^::1$)");

    public static String getUrl(HttpServletRequest req) {
        String query = req.getQueryString();
        return req.getRequestURI() + (isNotEmpty(query) ? "?" + query : "");
    }

    public static String getUrlFull(HttpServletRequest req) {
        String query = req.getQueryString();
        StringBuffer url = getRequestURLWithProxyCompatible(req);
        if (isNotEmpty(query))
            url.append('?').append(query);
        // 经常遇到POST请求，但是参数都拿不到，多有请求在log里面看都一样
        if ("POST".equalsIgnoreCase(req.getMethod())) {
            Enumeration<String> paramNames = req.getParameterNames();
            if (paramNames != null) {
                if (url.indexOf("?") < 0)
                    url.append('?');
                while (paramNames.hasMoreElements()) {
                    String paramName = paramNames.nextElement();
                    if (_.isNotEmpty(paramName)) {
                        String value = _.trimToEmpty(req.getParameter(paramName));
                        if (_.isNotEmpty(value)) {
                            url.append(paramName).append('=').append(_.urlencUtf8(value)).append('&');
                        }
                    }
                }
            }
        }
        return url.toString();
    }

    public static String getUrlMain(HttpServletRequest req) {
        int port = req.getServerPort();
        return getRequestSchemaWithProxyCompatible(req) + "://" + req.getServerName() + (port != 80 ? ":" + port : "");
    }

    private static String getRequestSchemaWithProxyCompatible(HttpServletRequest req) {
        String proxySchema = getProxySchema(req);
        if (_.nonEmpty(proxySchema))
            return proxySchema;
        return req.getScheme();
    }

    private static StringBuffer getRequestURLWithProxyCompatible(HttpServletRequest req) {
        String proxySchema = getProxySchema(req);
        if (_.nonEmpty(proxySchema)) {
            String reqSchema = req.getScheme().toLowerCase();
            if (!proxySchema.equals(reqSchema))
                return req.getRequestURL().replace(0, reqSchema.length(), proxySchema);
        }
        return req.getRequestURL();
    }

    private static String getProxySchema(HttpServletRequest req) {
        return _.trimToEmpty(req.getHeader("X-Forwarded-Proto")).toLowerCase();
    }


	/* ------------------------- cookie ------------------------- */

    public static String getCookie(HttpServletRequest req, String key) {
        Cookie[] cookies = req.getCookies();
        if (cookies == null)
            return null;
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(key))
                return cookie.getValue();
        }
        return null;
    }

    public static void setCookie(HttpServletResponse resp, String key, String value) {
        setCookie(resp, key, value, -1);
    }

    public static void setCookie(HttpServletResponse resp, String key, String value, int second) {
        setCookie(resp, key, value, second, null, null);
    }

    public static void setCookie(HttpServletResponse resp, String key, String value, int second, String path,
                                 String domain) {
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(second);
        if (isNotEmpty(path))
            cookie.setPath(path);
        if (isNotEmpty(domain))
            cookie.setDomain(domain);
        resp.addCookie(cookie);
    }

    public static void clearCookie(HttpServletResponse resp, String key) {
        clearCookie(resp, key, null, null);
    }

    public static void clearCookie(HttpServletResponse resp, String key, String path, String domain) {
        Cookie cookie = new Cookie(key, "");
        cookie.setMaxAge(0);
        if (isNotEmpty(path))
            cookie.setPath(path);
        if (isNotEmpty(domain))
            cookie.setDomain(domain);
        resp.addCookie(cookie);
    }

}
