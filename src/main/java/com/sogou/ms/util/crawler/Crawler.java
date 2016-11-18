package com.sogou.ms.util.crawler;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Function;
import com.sogou.ms.util.toolkit.AutoLog;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.execchain.MyMinimalHttpClient;
import org.apache.http.impl.execchain.resp.RespHandler;
import org.apache.http.impl.execchain.resp.RespHandlers;
import org.apache.http.impl.execchain.stat.CrawlLogHelper;
import org.apache.http.impl.execchain.stat.CrawlStage;
import org.apache.http.impl.execchain.stat.CrawlStopwatch;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.dom4j.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;

public final class Crawler {

	public static final Logger logger = LoggerFactory.getLogger(Crawler.class);

	/* ------------------------- builder ------------------------- */

	public static Crawler of(String name) {
		return of(name, new CrawlConfig());
	}
	public static Crawler of(String name, int connTimeout, int soTimeout) {
		return of(name, new CrawlConfig(connTimeout, soTimeout));
	}
	public static Crawler of(String name, int connTimeout, int soTimeout, int retry, int retryTimeout) {
		return of(name, new CrawlConfig(connTimeout, soTimeout, retry, retryTimeout));
	}
	public static Crawler of(String name, CrawlConfig config) {
		return new Crawler(name, config.importConfig(name));
	}

	/* ------------------------- init ------------------------- */

	private Crawler(String name, CrawlConfig config) {
		this.name = name;
		this.defaultCharset = config.defaultCharset;

		this.logSucc = config.logSucc;
		this.logFail = config.logFail;
		this.costLog = AutoLog.of("crawl." + this.name + ".cost");
		this.succLog = AutoLog.of("crawl." + this.name + ".succ");
		this.long200Log = AutoLog.of("crawl." + this.name + ".long200");
		this.long2000Log = AutoLog.of("crawl." + this.name + ".long2000");

		this.client = new MyMinimalHttpClient(config);
		logger.info("Crawler instance: {}=({},{},{},{},{},{})", name, config.connTimeout, config.soTimeout, config.maxTotalConn, config.maxConnPerRoute, config.retry, config.retryTimeout);
	}

	public final String name;
	public final Charset defaultCharset;
	public final boolean logSucc;
	public final boolean logFail;
	public final AutoLog costLog;
	public final AutoLog succLog;
	public final AutoLog long200Log;
	public final AutoLog long2000Log;

	public final MyMinimalHttpClient client;


	/* ------------------------- api ------------------------- */

	public Pair<Integer, String> put(String url, String entity, Charset entityCharset) {
		return put(url, new StringEntity(entity, entityCharset));
	}
	public Pair<Integer, String> put(String url, HttpEntity entity) {
		HttpPut req = new HttpPut(url);
		req.setEntity(entity);
		return put(req);
	}

	public Pair<Integer,String> put(HttpPut put){
		return exec(put,RespHandlers.pairHandler,null);
	}

	public Pair<Integer,String> put(HttpPut put,Charset charset){
		return exec(put,RespHandlers.pairHandler,charset);
	}


	public Pair<Integer, String> get(String url) {
		return get(url, null);
	}
	public Pair<Integer, String> get(String url, Charset charset) {
		return exec(new HttpGet(url), RespHandlers.pairHandler, charset);
	}

	public String getTrimmedContent(String url) {
		return getTrimmedContent(url, null);
	}
	public String getTrimmedContent(String url, Charset charset) {
		return exec(new HttpGet(url), RespHandlers.trimmedHandler, charset);
	}

	public Document getXml(String url) {
		return getXml(url, null);
	}
	public Document getXml(String url, Charset charset) {
		return exec(new HttpGet(url), RespHandlers.xmlHandler, charset);
	}

	public JsonNode getJson(String url) {
		return getJson(url, null);
	}
	public JsonNode getJson(String url, Charset charset) {
		return exec(new HttpGet(url), RespHandlers.jsonHandler, charset);
	}

	public <T> T getStream(String url, Function<InputStream, T> decoder) {
		return exec(new HttpGet(url), RespHandlers.withStream(decoder), null);
	}

	public <T> T getReader(String url, Function<Reader, T> decoder) {
		return getReader(url, null, decoder);
	}
	public <T> T getReader(String url, Charset charset, Function<Reader, T> decoder) {
		return exec(new HttpGet(url), RespHandlers.withReader(decoder), charset);
	}

	public <T> T getString(String url, Function<String, T> decoder) {
		return getString(url, null, decoder);
	}
	public <T> T getString(String url, Charset charset, Function<String, T> decoder) {
		return exec(new HttpGet(url), RespHandlers.withString(decoder), charset);
	}

	public Pair<Integer, String> post(String url, String entity, Charset entityCharset) {
		return post(url, new StringEntity(entity, entityCharset));
	}
	public Pair<Integer, String> post(String url, HttpEntity entity) {
		HttpPost req = new HttpPost(url);
		req.setEntity(entity);
		return post(req);
	}
	public Pair<Integer, String> post(HttpPost post,Charset respCharset) {
		return exec(post,RespHandlers.pairHandler,respCharset);
	}

	public Pair<Integer, String> post(HttpPost post) {
		return exec(post, RespHandlers.pairHandler, null);
	}

	/* ------------------------- impl ------------------------- */

	protected <T> T exec(HttpRequestBase req, RespHandler<T> handler, Charset defaultCharset) {
		final HttpContext ctx = new BasicHttpContext();
		final CrawlStopwatch watch = CrawlStopwatch.newInstance(ctx);
		CrawlLogHelper.setUrl(ctx, req.getURI().toString());

		boolean succ = false;
		T result;
		int code = RespHandlers.code_err;
		Exception e = null;

		try {
			CloseableHttpResponse resp = client.execute(req, ctx);
			code = resp.getStatusLine().getStatusCode();
			watch.log(CrawlStage.download_start.value);
			succ = RespHandlers.isSucc(resp);
			result = succ ? handler.applySucc(resp, defaultCharset) : handler.applyFail(code, null);
		} catch (Exception ex) {
			e = ex;
			result = handler.applyFail(RespHandlers.code_err, ex);
			succ = false;
		} finally {
			req.releaseConnection();
			req.abort();
		}

		watch.log(CrawlStage.download_end.value);
		int cost = (int) CrawlStopwatch.getPassedTime(watch);
		logStat(cost, succ);
		if (succ ? logSucc : logFail)
			CrawlLogHelper.logCrawl(watch, code, e, ctx);
		return result;
	}

	protected void logStat(int cost, boolean succ) {
		costLog.add(cost);
		succLog.addHit(succ);
		long200Log.addHit(cost > 200);
		long2000Log.addHit(cost > 2000);
	}

	@Override
	protected void finalize() throws Throwable {
		client.shutdown();
	}

}
