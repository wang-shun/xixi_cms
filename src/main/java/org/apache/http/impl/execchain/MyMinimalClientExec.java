package org.apache.http.impl.execchain;

import com.sogou.ms.util.crawler.CrawlConfig;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.*;
import org.apache.http.annotation.Immutable;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpRequestWrapper;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.protocol.RequestClientConnControl;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.ConnectionRequest;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.conn.ConnectionShutdownException;
import org.apache.http.impl.execchain.stat.CrawlStage;
import org.apache.http.protocol.*;
import org.apache.http.util.Args;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @see MinimalClientExec
 */
@Immutable
public final class MyMinimalClientExec {

	private final Log log = LogFactory.getLog(getClass());

	private final HttpRequestExecutor requestExecutor = new HttpRequestExecutor();
	private final ConnectionReuseStrategy reuseStrategy = DefaultConnectionReuseStrategy.INSTANCE;
	private final ConnectionKeepAliveStrategy keepAliveStrategy = DefaultConnectionKeepAliveStrategy.INSTANCE;
	private final HttpProcessor httpProcessor = new ImmutableHttpProcessor(
			new RequestContent(),
			new RequestTargetHost(),
			new RequestClientConnControl()
			// bell(2014-8): 内部服务用，不加 userAgent
			// , new RequestUserAgent(VersionInfo.getUserAgent("Apache-HttpClient", "org.apache.http.client", getClass()))
	);

	private final int connRequestTimeout;
	private final int connTimeout;
	private final int soTimeout;

	private final HttpClientConnectionManager connManager;
	private final MyHttpRequestRetryHandler retryHandler;

	public MyMinimalClientExec(final CrawlConfig conf, final HttpClientConnectionManager connManager) {
		this.connRequestTimeout = conf.connTimeout;
		this.connTimeout = conf.connTimeout;
		this.soTimeout = conf.soTimeout;

		Args.notNull(connManager, "Client connection manager");
		this.connManager = connManager;
		this.retryHandler = new MyHttpRequestRetryHandler(conf);
	}

	/* ------------------------- impl ------------------------- */

	static final CrawlStage __stage_round_start = CrawlStage.round_start;
	static final CrawlStage __stage_conn_start = CrawlStage.conn_start;
	static final CrawlStage __stage_conn_end = CrawlStage.conn_end;
	static final CrawlStage __stage_exec_start = CrawlStage.exec_start;
	static final CrawlStage __stage_exec_end = CrawlStage.exec_end;

	/**
	 * @see RetryExec
	 */
	public CloseableHttpResponse executeWithRetry(
			final HttpRoute route,
			final HttpRequestWrapper req,
			final HttpContext ctx,
			final HttpRequestBase execAware) throws IOException, HttpException {
		Args.notNull(route, "HTTP route");
		Args.notNull(req, "HTTP request");
		Args.notNull(ctx, "HTTP context");

		final Header[] origheaders = req.getAllHeaders();
		for (int execCount = 1; ; execCount++) {
			try {
				__stage_round_start.log(ctx);
				return this.execute(route, req, ctx, execAware);
			} catch (final IOException ex) {
				if (execAware != null && execAware.isAborted()) {
					throw ex;
				}
				if (retryHandler.retryRequest(ex, execCount, ctx)) {
					req.setHeaders(origheaders);
				} else {
					throw ex;
				}
			}
		}
	}


	private CloseableHttpResponse execute(
			final HttpRoute route,
			final HttpRequestWrapper req,
			final HttpContext ctx,
			final HttpRequestBase execAware) throws IOException, HttpException {

		rewriteRequestURI(req);

		final ConnectionRequest connRequest = connManager.requestConnection(route, null);
		if (execAware != null) {
			if (execAware.isAborted()) {
				connRequest.cancel();
				throw new RequestAbortedException("Request aborted");
			} else {
				execAware.setCancellable(connRequest);
			}
		}

		final HttpClientConnection managedConn;
		try {
			final int timeout = connRequestTimeout;
			managedConn = connRequest.get(timeout > 0 ? timeout : 0, TimeUnit.MILLISECONDS);
		} catch (final InterruptedException interrupted) {
			Thread.currentThread().interrupt();
			throw new RequestAbortedException("Request aborted", interrupted);
		} catch (final ExecutionException ex) {
			Throwable cause = ex.getCause();
			if (cause == null) {
				cause = ex;
			}
			throw new RequestAbortedException("Request execution failed", cause);
		}

		final ConnectionHolder releaseTrigger = new ConnectionHolder(log, connManager, managedConn);
		try {
			if (execAware != null) {
				if (execAware.isAborted()) {
					releaseTrigger.close();
					throw new RequestAbortedException("Request aborted");
				} else {
					execAware.setCancellable(releaseTrigger);
				}
			}

			if (!managedConn.isOpen()) {
				final int timeout = connTimeout;
				__stage_conn_start.log(ctx);
				this.connManager.connect(managedConn, route, timeout > 0 ? timeout : 0, ctx);
				__stage_conn_end.log(ctx);
				this.connManager.routeComplete(managedConn, route, ctx);
			}
			final int timeout = soTimeout;
			if (timeout >= 0) {
				managedConn.setSocketTimeout(timeout);
			}

			HttpHost target = null;
			final HttpRequest original = req.getOriginal();
			if (original instanceof HttpUriRequest) {
				final URI uri = ((HttpUriRequest) original).getURI();
				if (uri.isAbsolute()) {
					target = new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme());
				}
			}
			if (target == null) {
				target = route.getTargetHost();
			}

			ctx.setAttribute(HttpCoreContext.HTTP_TARGET_HOST, target);
			ctx.setAttribute(HttpCoreContext.HTTP_REQUEST, req);
			ctx.setAttribute(HttpCoreContext.HTTP_CONNECTION, managedConn);
			ctx.setAttribute(HttpClientContext.HTTP_ROUTE, route);

			httpProcessor.process(req, ctx);
			__stage_exec_start.log(ctx);
			final HttpResponse response = requestExecutor.execute(req, managedConn, ctx);
			__stage_exec_end.log(ctx);
			httpProcessor.process(response, ctx);

			// The connection is in or can be brought to a re-usable state.
			if (reuseStrategy.keepAlive(response, ctx)) {
				// Set the idle duration of this connection
				final long duration = keepAliveStrategy.getKeepAliveDuration(response, ctx);
                // 2015-01-01 01:33:45
                // 这里有个坑，默认Response里面如果没有Keep-Alive的时候的KeepAliveDuration取到的duration是-1
                // 以前这里不管duration的值是什么都会设置setValidFor和markReuseable
                // 但是这时候下一次再链接的时候execute就会报错，所以这里可以两种解决方案
                // 方案一：根据duration的取值是否大于0，来判断是否markReusable
                // 方案二：可以duration固定一个取值，这样也ok
                if(duration>0) {
                    releaseTrigger.setValidFor(duration, TimeUnit.MILLISECONDS);
                    releaseTrigger.markReusable();
                } else {
					//tracy(2015-5-22): 如果默认没有返回keep-alive-timeout就不要用长连接了
                    releaseTrigger.markNonReusable();
                }
			} else {
				releaseTrigger.markNonReusable();
			}

			// check for entity, release connection if possible
			final HttpEntity entity = response.getEntity();
			if (entity == null || !entity.isStreaming()) {
				// connection not needed and (assumed to be) in re-usable state
				releaseTrigger.releaseConnection();
				return new HttpResponseProxy(response, null);
			} else {
				return new HttpResponseProxy(response, releaseTrigger);
			}
		} catch (final ConnectionShutdownException ex) {
			final InterruptedIOException ioex = new InterruptedIOException("Connection has been shut down");
			ioex.initCause(ex);
			throw ioex;
		} catch (final HttpException | IOException | RuntimeException ex) {
			releaseTrigger.abortConnection();
			throw ex;
		}
	}
	static void rewriteRequestURI(
			final HttpRequestWrapper request) throws ProtocolException {
		try {
			URI uri = request.getURI();
			if (uri != null) {
				// Make sure the request URI is relative
				if (uri.isAbsolute()) {
					uri = URIUtils.rewriteURI(uri, null, true);
				} else {
					uri = URIUtils.rewriteURI(uri);
				}
				request.setURI(uri);
			}
		} catch (final URISyntaxException ex) {
			throw new ProtocolException("Invalid URI: " + request.getRequestLine().getUri(), ex);
		}
	}

}
