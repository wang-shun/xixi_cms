package org.apache.http.impl.execchain.resp;

import com.sogou.ms.util.crawler.Crawler;
import org.apache.http.HttpResponse;

import java.nio.charset.Charset;

public abstract class RespHandler<T> {

	public abstract T applySucc(HttpResponse resp, Charset defaultCharset) throws Exception;

	public T applyFail(int httpCode, Exception e) {
		Crawler.logger.error("resp handle err: " + e);
		return null;
	}

}
