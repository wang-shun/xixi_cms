package com.sogou.ms.util.infrastructure.web.el;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.sogou.ms.util._.*;


public class UrlUtil {

	/* ------------------------- get current params ------------------------- */

	public static String currParams(HttpServletRequest request, String paramKeys) {
		StringBuilder out = new StringBuilder();
		boolean needSeparator = false;
		for (String key : paramKeys.split("\\W+")) {
			if (nonEmpty(key)) {
				String val = request.getParameter(key);
				if (nonEmpty(val)) {
					if (needSeparator)
						out.append('&');
					out.append(key).append('=').append(urlenc(val));
					needSeparator = true;
				}
			}
		}
		return out.toString();
	}

	/* ------------------------- join params ------------------------- */

	public static String buildParam(Object[] params) {
		return doJoinParams(0, params);
	}
	// initMark = 0=null, 1=?, 2=&
	private static String doJoinParams(int mark, Object[] params) {
		Map<String, String> paramMap = asStringMap(params);
		StringBuilder out = new StringBuilder();
		for (Map.Entry<String, String> entry : paramMap.entrySet()) {
			if (!isEmpty(entry.getValue())) {
				out.append(mark == 0 ? "" : mark == 1 ? "?" : "&");
				out.append(entry.getKey());
				out.append("=");
				out.append(urlenc(entry.getValue()));
				mark = 2;
			}
		}
		return out.toString();
	}

	/* ------------------------- adjust params ------------------------- */

	public static String removeParam(String url, String key) {
		if (isEmpty(key)) throw new IllegalArgumentException("key is empty");
		int pathEnd = url.indexOf('?');
		if (pathEnd < 0) return url;
		final int urlLen = url.length();
		if (pathEnd + 1 == urlLen) return url.substring(0, urlLen - 1); // [?]$ => 去除最末问号
		final int keyLen = key.length();

		StringBuilder out = new StringBuilder(urlLen);
		out.append(url.substring(0, pathEnd));
		boolean firstParam = true;
		for (String part : url.substring(pathEnd + 1).split("&(amp;)?")) {
			if (part.startsWith(key) &&
					(part.length() == keyLen || part.charAt(keyLen) == '='))
				continue;

			out.append(firstParam ? '?' : '&');
			out.append(part);
			firstParam = false;
		}
		return out.toString();
	}

	public static String removeParam(String url, Set<String> keys) {
		int pathEnd = url.indexOf('?');
		if (pathEnd < 0) return url;
		final int urlLen = url.length();
		if (pathEnd + 1 == urlLen) return url.substring(0, urlLen - 1); // [?]$ => 去除最末问号

		StringBuilder out = new StringBuilder(urlLen);
		out.append(url.substring(0, pathEnd));
		boolean firstParam = true;
		for (String part : url.substring(pathEnd + 1).split("&(amp;)?")) {
			int keyEnd = part.indexOf('=');
			boolean remove = keyEnd > 0 ? keys.contains(part.substring(0, keyEnd)) :
					keyEnd < 0 ? keys.contains(part) : true;
			if (!remove) {
				out.append(firstParam ? '?' : '&');
				out.append(part);
				firstParam = false;
			}
		}
		return out.toString();
	}

	public static String overrideParam(String url, String key, String value) {
		String removed = removeParam(url, key);
		return addParam(removed, key, value);
	}
	public static String overrideParam(String url, String k1, String v1, String k2, String v2) {
		Map<String, String> paramMap = asStringMap(k1, v1, k2, v2);
		String removed = removeParam(url, paramMap.keySet());
		return addParam(removed, paramMap);
	}
	public static String overrideParam(String url, String k1, String v1, String k2, String v2, String k3, String v3) {
		Map<String, String> paramMap = asStringMap(k1, v1, k2, v2, k3, v3);
		String removed = removeParam(url, paramMap.keySet());
		return addParam(removed, paramMap);
	}

	private static String addParam(String url, String key, String value) {
		return isEmpty(value) ? url : (url + (url.contains("?") ? '&' : '?') + key + '=' + value);
	}
	private static String addParam(String url, Map<String, String> paramMap) {
		boolean quesMarkExists = url.contains("?");
		for (Map.Entry<String, String> entry : paramMap.entrySet()) {
			if (nonEmpty(entry.getValue())) {
				url += (quesMarkExists ? '&' : '?') + entry.getKey() + '=' + urlenc(entry.getValue());
				quesMarkExists = true;
			}
		}
		return url;
	}

    public static Map<String,String> paramsMap(String url){
        Map<String,String> paramsMap = new HashMap<>(16);
        int splitPos = url.indexOf("?");
        if( splitPos != -1 && splitPos < url.length() ){
            String param = url.substring(splitPos + 1);
            if( nonEmpty(param) ){
                String[] pairs = param.split("&(amp;)?");
                for( String pair : pairs ){
                    String[] item = pair.split("=");
                    if( item.length == 2 ){
                        paramsMap.put(item[0], item[1]);
                    }
                }
            }
        }
        return paramsMap;
    }

    public static String unUrlenc( String key, String charset ){
        try{
            return URLDecoder.decode(key,charset);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return key;
    }

}
