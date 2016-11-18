package web.util;

import com.sogou.ms.util._;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CookieUtil {

	static final Logger logger = LoggerFactory.getLogger(CookieUtil.class);


    public static String getCookie(HttpServletRequest request, String cookieName, String defaultVal){
        String cookie = getCookie(request, cookieName);
        return _.isEmpty(cookie)?defaultVal:cookie;
    }


	public static String getCookie(HttpServletRequest request, String cookieName) {
		try {
			Cookie[] cookies = request.getCookies();
			if (cookies != null) {
				for (int i = 0; i < cookies.length; i++) {
					String cookieVal = cookies[i].getValue();
					if (cookieVal == null)
						continue;
					if (cookieName.equals(cookies[i].getName())) {
						return cookieVal;
					}
				}
			}
		} catch (Exception e) {
			logger.error("impossible in getCookie(). name:" + cookieName);
		}
		return null;
	}

	public static void setCookie(HttpServletResponse response, String cookieName, String cookieVal) {
		try {
			Cookie cookie = new Cookie(cookieName, cookieVal);
			cookie.setPath("/");
			cookie.setDomain(".sogou.com");
			cookie.setMaxAge(365 * 24 * 60 * 60);
			cookie.setVersion(1);
			response.addCookie(cookie);
		} catch (Exception e) {
			logger.error("impossible in setCookie(). name:" + cookieName);
		}
	}

	public static void deleteCookie(HttpServletResponse resp, String cookieName){
		try{
			Cookie cookie = new Cookie(cookieName,"");
			cookie.setPath("/");
			cookie.setDomain(".sogou.com");
			cookie.setMaxAge(0);
			resp.addCookie(cookie);
		}catch (Exception e){
			logger.error("delete cookie fail. name:" + cookieName);
		}
	}


}
