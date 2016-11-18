package com.sogou.ms.util.crawler;

import com.sogou.ms.util._;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;

import java.nio.charset.Charset;
import java.util.Map;

/**
 * User: madtracy
 * Time: 2016/10/20.
 */
public class StringEntityBuilder {

	public static HttpEntity build(String content){
		return build(content,_.charsetUtf8);
	}
	public static HttpEntity build(String content,Charset charset){
		return MultipartEntityBuilder.create().setCharset(charset)
				.addTextBody("",content,ContentType.create(ContentType.TEXT_PLAIN.getMimeType(), charset))
				.build();
	}


	public static HttpEntity build(Charset charset,String... args){
		MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create().setCharset(charset);
		ContentType contentType = ContentType.create(ContentType.APPLICATION_FORM_URLENCODED.getMimeType(), charset);
		for(int i=0;i<=args.length-2;i+=2){
			multipartEntityBuilder.addTextBody(args[i],args[i+1],contentType);
		}
		return multipartEntityBuilder.build();
	}

	public static HttpEntity build(Charset charset,Map<String, String> params){
		MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create().setCharset(charset);
		ContentType contentType = ContentType.create(ContentType.APPLICATION_FORM_URLENCODED.getMimeType(), charset);
		for(String key : params.keySet()){
			multipartEntityBuilder.addTextBody(key,params.get(key),contentType);
		}
		return multipartEntityBuilder.build();
	}


}
