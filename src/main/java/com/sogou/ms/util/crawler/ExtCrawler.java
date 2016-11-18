package com.sogou.ms.util.crawler;

import com.sogou.ms.util._;
import com.sogou.ms.util.infrastructure.Config;
import com.sogou.ms.util.toolkit.AutoLog;
import com.sogou.ms.util.toolkit.MyExecutor;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

/**
 * User:Jarod
 */
public class ExtCrawler {

	private static Logger logger = LoggerFactory.getLogger(ExtCrawler.class);

	public static ExtCrawler of(String name, int totalTimeout) {
		totalTimeout = _.toInt(Config.of(name + ".total.timeout", _.toStr(totalTimeout)).get(), totalTimeout);
		return new ExtCrawler(name, Crawler.of(name), totalTimeout);
	}

	private ExtCrawler(String name, Crawler crawler, int totalTimeout) {
		this.name = name;
		this.crawler = crawler;
		this.totalTimeout = totalTimeout;
		logger.info("ExtCrawler instance: {}({})", this.name, this.totalTimeout);
	}

	private String name;
	private int totalTimeout;
	private Crawler crawler;
	private static AutoLog ext_crawler_future_error = AutoLog.of("ext.crawler.future.error");
	private static AutoLog ext_crawler_future_timeout = AutoLog.of("ext.crawler.future.timeout");

	public Pair<Integer, String> get(final String url) {
		Future<Pair<Integer, String>> crawlerFuture = null;
		try {
			crawlerFuture = MyExecutor.instance()
					.submit("Ext.Crawler", new Callable<Pair<Integer, String>>() {
						@Override
						public Pair<Integer, String> call() throws Exception {
							return crawler.get(url);
						}
					});
		} catch (Exception ex) {
			ext_crawler_future_error.add(1);
			return Pair.of(901, "");
		}
		ext_crawler_future_error.add(0);

		Exception exception = null;
		if (crawlerFuture != null) {
			try {
				Pair<Integer, String> content = crawlerFuture.get(totalTimeout, TimeUnit.MILLISECONDS);
				ext_crawler_future_timeout.add(0);
				return content;
			} catch (InterruptedException e) {
				exception = e;
			} catch (ExecutionException e) {
				exception = e;
			} catch (TimeoutException e) {
				exception = e;
			} finally {
				crawlerFuture.cancel(true);
			}
		}
		logException(exception);
		ext_crawler_future_timeout.add(1);
		if (exception == null) {
			return Pair.of(900, "");
		} else {
			return Pair.of(900, exception.toString());
		}
	}

	private static void logException(Exception ex) {
		if (ex != null) {
			logger.error("ExtCrawler-Error:{}", ex.toString());
		}
	}

	public String getContent(String url) {
		Pair<Integer, String> pair = this.get(url);
		if (pair != null && pair.getLeft() == 200) {
			return _.trimToEmpty(pair.getRight());
		}
		return "";
	}
}
