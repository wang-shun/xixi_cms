package org.apache.http.impl.execchain;

import com.sogou.ms.util.TimeUtil;
import com.sogou.ms.util.crawler.CrawlConfig;
import com.sogou.ms.util.toolkit.MyExecutor;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.annotation.ThreadSafe;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpRequestWrapper;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.Args;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

@ThreadSafe
public final class MyMinimalHttpClient {

	// static final Logger logger = LoggerFactory.getLogger(MyMinimalHttpClient.class);

	public final PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
	private final MyMinimalClientExec requestExecutor;

	public MyMinimalHttpClient(CrawlConfig conf) {
		this.connManager.setMaxTotal(conf.maxTotalConn);
		this.connManager.setDefaultMaxPerRoute(conf.maxConnPerRoute);

		this.requestExecutor = new MyMinimalClientExec(conf, connManager);

		/*MyExecutor.timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				System.out.println("http client pool checking start");
				MyMinimalHttpClient.this.connManager.closeExpiredConnections();
				MyMinimalHttpClient.this.connManager.closeIdleConnections(30, TimeUnit.SECONDS);
				System.out.println("http client pool checking fin");
			}
		}, 20 * TimeUtil.second, 10 * TimeUtil.second);*/
	}

	public void shutdown() {
		this.connManager.shutdown();
	}

	/* ------------------------- exec ------------------------- */

	public CloseableHttpResponse execute(HttpRequestBase req, HttpContext ctx) throws HttpException, IOException {
		Args.notNull(req, "HTTP request");
		Args.notNull(ctx, "HTTP context");
		HttpHost target = URIUtils.extractHost(req.getURI());
		Args.notNull(target, "Target host");

		HttpRequestWrapper wrapper = HttpRequestWrapper.wrap(req);
		HttpRoute route = new HttpRoute(target);
		RequestConfig config = req.getConfig();
		if (config != null)
			ctx.setAttribute(HttpClientContext.REQUEST_CONFIG, config);

		return this.requestExecutor.executeWithRetry(route, wrapper, ctx, req);
	}

}
