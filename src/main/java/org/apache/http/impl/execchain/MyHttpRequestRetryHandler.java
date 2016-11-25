package org.apache.http.impl.execchain;

import com.sogou.ms.util.crawler.CrawlConfig;
import org.apache.http.impl.execchain.stat.CrawlLogHelper;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.methods.HttpRequestWrapper;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.execchain.stat.CrawlStopwatch;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;
import org.apache.http.util.Args;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ConnectException;
import java.net.UnknownHostException;

import static com.sogou.ms.util._.sleep;

/**
 * 添加两个策略：1.如果超过n时间则不再重试、2.每次重试间sleep一定时间
 * @see org.apache.http.impl.client.DefaultHttpRequestRetryHandler
 */
public final class MyHttpRequestRetryHandler implements HttpRequestRetryHandler {

	public static long sleepBeforeRetry = 10;

	final int retryCount;
	final long retryTimeout;
	final boolean logRetry;

	public MyHttpRequestRetryHandler(CrawlConfig config) {
		this.retryCount = config.retry;
		this.retryTimeout = config.retryTimeout;
		this.logRetry = config.logRetry;
	}

	@Override
	public boolean retryRequest(IOException e, int execCount, HttpContext ctx) {
		Args.notNull(e, "Exception parameter");
		Args.notNull(ctx, "HTTP context");

		final CrawlStopwatch watch = CrawlStopwatch.from(ctx);
		long passedTime = CrawlStopwatch.getPassedTime(watch);
		boolean retry = false;
		if(passedTime < this.retryTimeout){
			//tracy(2015-5-22): 第一次抓取的时候，如果是NoHttpResponseException,表明请求没有发送出去，不用检测retryCount
			if(execCount==1 && (e instanceof NoHttpResponseException)){
				// bell(2013-9): 关于org.apache.http.NoHttpResponseException: The target server failed to respond
				// 当对方由于超时等，主动将连接关闭，此时我们并不知道，此时发请求会有这种异常，且响应时间超级短(通常1~2ms)
				// 而HttpClient默认会将post接口理解成非idempotent，认为这种情况已经sent，所以不重发，特此修正
				// bell(2013-9): 之前条件为 passedTime<5ms，线下虚机有5%概率未触发重试
				// 自觉过于小心谨慎，考虑到news有重试两次仍然不行的现象，不检查耗时了
				retry=true;
			}else{
				retry = execCount <= this.retryCount && defaultRetryRequest(e, ctx);
			}
			if (retry) {
				if (logRetry)
					CrawlLogHelper.logCrawl(watch, 0, e, ctx);
				if (sleepBeforeRetry > 0)
					sleep(sleepBeforeRetry);
				CrawlStopwatch.setExecCount(watch, execCount);
			}
		}
		return retry;
	}

	private boolean defaultRetryRequest(final IOException exception, final HttpContext ctx) {

		if (exception instanceof InterruptedIOException) return false; // Timeout
		if (exception instanceof UnknownHostException) return false; // Unknown host
		if (exception instanceof ConnectException) return false; // Connection refused
		if (exception instanceof SSLException) return false; // SSL handshake exception


		final HttpRequest request = (HttpRequest) ctx.getAttribute(HttpCoreContext.HTTP_REQUEST);
		if (requestIsAborted(request))
			return false;

		// bell(2013-9): 关于org.apache.http.NoHttpResponseException: The target server failed to respond
		// 当对方由于超时等，主动将连接关闭，此时我们并不知道，此时发请求会有这种异常，且响应时间超级短(通常1~2ms)
		// 而HttpClient默认会将post接口理解成非idempotent，认为这种情况已经sent，所以不重发，特此修正
		// bell(2013-9): 之前条件为 passedTime<5ms，线下虚机有5%概率未触发重试
		// 自觉过于小心谨慎，考虑到news有重试两次仍然不行的现象，不检查耗时了
		if (exception instanceof NoHttpResponseException)
			return true;

		// Retry if the request is considered idempotent
		if (handleAsIdempotent(request))
			return true;

		boolean sent = Boolean.TRUE == ctx.getAttribute(HttpCoreContext.HTTP_REQ_SENT);
		return !sent;
	}

	private boolean requestIsAborted(final HttpRequest request) {
		HttpRequest req = request;
		if (request instanceof HttpRequestWrapper) { // does not forward request to original
			req = ((HttpRequestWrapper) request).getOriginal();
		}
		return (req instanceof HttpUriRequest && ((HttpUriRequest) req).isAborted());
	}
	private boolean handleAsIdempotent(final HttpRequest request) {
		return !(request instanceof HttpEntityEnclosingRequest);
	}

}
