package com.sogou.ms.util.crawler;

import static com.sogou.ms.util._.*;
import org.apache.http.HttpEntity;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;

import java.nio.charset.Charset;
import java.util.Map;

/**
 * User:Jarod
 */
public class PostEntity {

	public static HttpEntity of(String firstKey, String firstValue,
								String... args) {
		return of(charsetUtf8, firstKey, firstValue, args);
	}

	public static HttpEntity of(Charset charset, String firstKey,
								String firstValue, String... args) {
		StringBuilder sb = new StringBuilder();
		if (args.length % 2 != 0)
			throw new IllegalArgumentException();
		sb.append(firstKey);
		sb.append('=');
		sb.append(urlenc(firstValue, charset.name()));
		for (int i = 0; i <= args.length - 2; i += 2) {
			sb.append('&');
			sb.append(args[i]);
			sb.append('=');
			sb.append(urlenc(args[i + 1], charset.name()));
		}

		String content = sb.toString();
		return new StringEntity(content, ContentType.create(
				URLEncodedUtils.CONTENT_TYPE, charset));
	}

	public static HttpEntity of(Charset charset, Map<String, String> params) {
		StringBuilder sb = new StringBuilder();
		for (String pname : params.keySet()) {
			sb.append(pname).append('=').append(urlenc(params.get(pname), charset.name())).append('&');
		}
		sb = sb.deleteCharAt(sb.length() - 1);
		String content = sb.toString();
		return new StringEntity(content, ContentType.create(
				URLEncodedUtils.CONTENT_TYPE, charset));
	}

}
