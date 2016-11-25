package org.apache.http.impl.execchain.stat;


import com.sogou.ms.util.crawler.Crawler;
import org.apache.http.impl.execchain.stat.CrawlStopwatch;
import org.apache.http.protocol.HttpContext;

import static com.sogou.ms.util._.*;

public class CrawlLogHelper {

	public static void setUrl(HttpContext ctx, String url) {
		ctx.setAttribute("url", url);
	}
	public static String getUrl(HttpContext ctx) {
		return (String) ctx.getAttribute("url");
	}

	public static void logCrawl(CrawlStopwatch watch, int httpCode, Exception e, HttpContext ctx) {
		int round = (int) watch.timestamps[CrawlStopwatch.maxStage + 2];
		long cost = System.currentTimeMillis() - watch.timestamps[0];
		String timelog = watch.toString();
		String eStr = e != null ? (", err:" + e) : "";
		Crawler.logger.info(f("[CrawlLog] (%s) cost:%s, timelog:%s, code:%s%s, url:%s",
				round, cost, timelog, httpCode, eStr, getUrl(ctx)));
	}

}
