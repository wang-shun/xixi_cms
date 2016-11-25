package org.apache.http.impl.execchain.stat;

import org.apache.http.protocol.HttpContext;

public final class CrawlStopwatch {

	public static CrawlStopwatch newInstance(HttpContext ctx) {
		CrawlStopwatch watch = new CrawlStopwatch();
		ctx.setAttribute("timelog", watch);
		return watch;
	}
	public static CrawlStopwatch from(HttpContext ctx) {
		return ctx != null ? (CrawlStopwatch) ctx.getAttribute("timelog") : null;
	}

	public static long getPassedTime(CrawlStopwatch watch) {
		return watch == null ? 0 : (System.currentTimeMillis() - watch.timestamps[0]);
	}
	public static void setExecCount(CrawlStopwatch watch, int execCount) {
		if (watch != null)
			watch.timestamps[CrawlStopwatch.maxStage + 2] = execCount;
	}

	/* ------------------------- init ------------------------- */

	// bell(2012-12): 留作public，预防灵活需求
	public static final int maxStage = CrawlStage._lastValue;
	public final long[] timestamps; // initTime, stagesTime..., currentStage, retryTimes

	CrawlStopwatch() {
		this.timestamps = new long[maxStage + 3];
		this.timestamps[0] = System.currentTimeMillis();
		this.timestamps[maxStage + 1] = 0; // stage = 0
		this.timestamps[maxStage + 2] = 0; // retry = 0
	}

	/* ------------------------- impl ------------------------- */

	public void log(int stage) {
		if (stage <= 0 || stage > maxStage)
			throw new IllegalArgumentException("illegal stage: " + stage);

		this.timestamps[stage] = System.currentTimeMillis();
		this.timestamps[maxStage + 1] = stage;

		// bell(2013-9): 重试会重新触发stage1
		if (stage == 1)
			for (int i = 2; i <= maxStage; i++)
				this.timestamps[i] = 0;
	}

	/**
	 * format: cost|stage|cost1|cost2|cost3
	 * 13_<10>_1|0|0|h0|c2|0|0|w9|0|d1
	 */
	@Override
	public String toString() {
		long lastTime = this.timestamps[0];
		StringBuilder sb = new StringBuilder();
		sb.append(System.currentTimeMillis() - lastTime);
		sb.append("_<");
		sb.append(this.timestamps[maxStage + 1]);

		int lastValue = maxStage;
		while (this.timestamps[lastValue] == 0 && lastValue > 0)
			lastValue--;

		for (int i = 1; i <= lastValue; i++) {
			long curTime = this.timestamps[i];
			long stamp;
			if (curTime <= 0)
				stamp = -1;
			else {
				stamp = curTime - lastTime;
				lastTime = curTime;
			}
			sb.append(i == 1 ? ">_" : "|");
			if (i == CrawlStage.conn_end.value)
				sb.append("c");
			if (i == CrawlStage.exec_end.value)
				sb.append("w");
			if (i == CrawlStage.download_end.value)
				sb.append("d");
			sb.append(stamp);
		}

		return sb.toString();
	}

}
