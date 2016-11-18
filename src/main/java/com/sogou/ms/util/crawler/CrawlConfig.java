package com.sogou.ms.util.crawler;

import com.sogou.ms.util.infrastructure.Config;
import org.apache.http.Consts;

import java.nio.charset.Charset;

import static com.sogou.ms.util._.*;

public final class CrawlConfig {

	public static final int defMaxTotalConn = 1000;
	public static final int defMaxConnPerRoute = 300;
	public static final int defConnTimeout = 3000;
	public static final int defSoTimeout = 6000;

	public static final int defRetry = 2;
	public static final int defRetryTimeout = 100;

	public static final Charset defCharset = Consts.UTF_8;

	public static final boolean defLogSucc = true;
	public static final boolean defLogRetry = true;
	public static final boolean defLogFail = true;

	/* ------------------------- instance ------------------------- */

	public CrawlConfig() {
		this(defMaxTotalConn, defMaxConnPerRoute, defConnTimeout, defSoTimeout,
				defRetry, defRetryTimeout, defCharset,
				defLogSucc, defLogRetry, defLogFail);
	}
	public CrawlConfig(int connTimeout, int soTimeout) {
		this(defMaxTotalConn, defMaxConnPerRoute, connTimeout, soTimeout,
				defRetry, defRetryTimeout, defCharset,
				defLogSucc, defLogRetry, defLogFail);
	}
	public CrawlConfig(int connTimeout, int soTimeout, int retry, int retryTimeout) {
		this(defMaxTotalConn, defMaxConnPerRoute, connTimeout, soTimeout,
				retry, retryTimeout, defCharset,
				defLogSucc, defLogRetry, defLogFail);
	}
	public CrawlConfig(int maxTotalConn, int maxConnPerRoute, int connTimeout, int soTimeout,
					   int retry, int retryTimeout, Charset charset,
					   boolean logSucc, boolean logRetry, boolean logFail) {
		this.connTimeout = connTimeout;
		this.soTimeout = soTimeout;
		this.maxTotalConn = maxTotalConn;
		this.maxConnPerRoute = maxConnPerRoute;

		this.retry = retry;
		this.retryTimeout = retryTimeout;

		this.defaultCharset = charset;

		this.logSucc = logSucc;
		this.logRetry = logRetry;
		this.logFail = logFail;
	}

	public final int maxTotalConn;
	public final int maxConnPerRoute;
	public final int connTimeout;
	public final int soTimeout;

	public final int retry;
	public final long retryTimeout;

	public final boolean logRetry;
	public final boolean logFail;
	public final boolean logSucc;

	public final Charset defaultCharset;

	public CrawlConfig importConfig(String name) {
		int maxTotalConn = toInt(Config.of(name + ".max.total.conn", this.maxTotalConn).get());
		int maxConnPerHost = toInt(Config.of(name + ".max.conn.per.host", this.maxConnPerRoute).get());
		int connTimeout = toInt(Config.of(name + ".conn.timeout", this.connTimeout).get());
		int soTimeout = toInt(Config.of(name + ".so.timeout", this.soTimeout).get());
		int retry = toInt(Config.of(name + ".retry", this.retry).get());
		int retryTimeout = toInt(Config.of(name + ".retry.timeout", this.retryTimeout).get());
		boolean logSucc = "true".equalsIgnoreCase(Config.of(name+".logSucc",this.logSucc).get());

		return new CrawlConfig(maxTotalConn, maxConnPerHost, connTimeout, soTimeout, retry, retryTimeout,
				this.defaultCharset, logSucc, this.logRetry, this.logFail);
	}

}
