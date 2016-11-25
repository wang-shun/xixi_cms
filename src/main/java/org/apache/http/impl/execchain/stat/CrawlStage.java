package org.apache.http.impl.execchain.stat;

import com.sogou.ms.util.crawler.Crawler;
import org.apache.http.impl.execchain.MyMinimalClientExec;
import org.apache.http.protocol.HttpContext;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public final class CrawlStage {

	/* ------------------------- constants ------------------------- */

	private static int idx = 0;

	// ** cost: retry, redirect

	@UsedBy(MyMinimalClientExec.class)
	public static final CrawlStage round_start = new CrawlStage(++idx); // per request start(redirect considered)

	// ** cost: pick entry from connManager

	@UsedBy(MyMinimalClientExec.class)
	public static final CrawlStage conn_start = new CrawlStage(++idx); // conn succ.

	// ** cost: dns lookup, conn

	@UsedBy(MyMinimalClientExec.class)
	public static final CrawlStage conn_end = new CrawlStage(++idx); // conn succ.
	// err: org.apache.http.conn.ConnectTimeoutException: Connect to weibo.com:80 timed out
	// err: java.net.UnknownHostException: w

	// * cost: prepare request

	@UsedBy(MyMinimalClientExec.class)
	public static final CrawlStage exec_start = new CrawlStage(++idx); //

	// ** cost: recieve response

	@UsedBy(MyMinimalClientExec.class)
	public static final CrawlStage exec_end = new CrawlStage(++idx); //

	@UsedBy(Crawler.class)
	public static final CrawlStage download_start = new CrawlStage(++idx); //

	// ** cost: download left body

	@UsedBy(Crawler.class)
	public static final CrawlStage download_end = new CrawlStage(++idx); //

	static final int _lastValue = download_end.value;

	/* ------------------------- impl ------------------------- */

	public final int value;
	private CrawlStage(int value) {
		this.value = value;
	}

	public void log(HttpContext context) {
		CrawlStopwatch watch = CrawlStopwatch.from(context);
		if (watch != null)
			watch.log(this.value);
	}

	@Target(ElementType.FIELD)
	@Retention(RetentionPolicy.SOURCE)
	@interface UsedBy {
		Class<?>[] value();
	}

}
