package com.sogou.ms.util;

public class ThreadUtil {

	public static boolean enabled = true;

	/** bell(2013-2): Java中，没有真正能中止其他线程操作的方法，thread.interrupt()只能中止wait/sleep状态和channel。
	 * 所以，耗时较长操作需主动发起检查，使线程收到interrupt后能中止执行 */
	public static void checkInterrupted() {
		if (enabled && Thread.currentThread().isInterrupted())
			throw new ThreadDeath();
	}

}
