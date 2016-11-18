package com.sogou.ms.util.toolkit;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 在Executors.newFixedThreadPool基础上进行以下修正：
 * + 线程池使用daemon线程，main()结束时能自动退出
 * + 带type提交任务，可查看是哪些任务占着线程池，有分type的计时记录
 * + DiscardPolicy带log，开始discard时，说明压力n大，打一下当前都哪些任务在执行
 * + 增加延时调用(使用Timer实现)
 */
public final class MyExecutor extends ThreadPoolExecutor {

	static final Logger logger = LoggerFactory.getLogger(MyExecutor.class);

	// bell(2012-12): 以下参数的考虑场景：
	// 日常保持core个线程
	// 当core个线程处理不过来时，可能是突发，用BlockingQueue平滑一下压力
	// 当queue放满时，说明真正的压力到来了，最多开max个线程，全力处理
	// bell(2013-8): 想修改这些参数时，可以更新这些变量后，restart线程池
	public static int executorQueueSize = 32;
	public static int executorCoreThreads = 64;
	public static int executorMaxThreads = 320; // 100
	// bell(2012-12): core和max之间的线程多久没活干就退
	public static long executorKeepAliveMS = 9000;

	/* ------------------------- api.misc ------------------------- */

	public static MyExecutor instance() {
		return SingletonHolder.instance;
	}
	public static void restart() {
		SingletonHolder.restart();
	}

	public Stat getStat() {
		return this.stat;
	}
	public void resetStat() {
		this.stat = new Stat();
	}

	public Future<?> submit(String type, Runnable task) {
		return this.submit(type, Executors.callable(task));
	}
	/* 若提交任务失败,该方法会抛出拒绝异常 */
	public <T> Future<T> submit(String type, Callable<T> task) {
		Callable<T> future = new TaskWrapper<T>(type, stat, task);
		return super.submit(future);
	}
	public void schedule(String type, Runnable task, long delay) {
		timer.schedule(new WrapperTimerTask(type, task), delay);
	}

	/* ------------------------- impl ------------------------- */

	@Override
	@Deprecated
	public <T> Future<T> submit(Callable<T> task) {
		if (task == null)
			throw new NullPointerException();
		return this.submit("anonym1", task);
	}
	@Override
	@Deprecated
	public Future<?> submit(Runnable task) {
		if (task == null)
			throw new NullPointerException();
		return this.submit("anonym2", Executors.callable(task));
	}
	@Override
	@Deprecated
	public <T> Future<T> submit(Runnable task, T result) {
		if (task == null)
			throw new NullPointerException();
		return this.submit("anonym3", Executors.callable(task, result));
	}

	Stat stat = new Stat();

	// bell(2012-8): hack
	// 由于newTaskFor仅支持一个参数，无法传入name，所以预先将FutureTask准备好，在newTaskFor直接返回
	@Override
	protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
		if (callable == null) {
			throw new NullPointerException();
		}
		if (callable instanceof TaskWrapper) {
			return (TaskWrapper<T>) callable;
		}
		return super.newTaskFor(callable);
	}
	public static final class TaskWrapper<V> extends FutureTask<V> implements Callable<V> {
		final String type;
		final Stat _stat;
		final long[] startTime;
		// bell(2012-12): 之所以如此纠结地定义两个构造函数，全怪以下(而且super()必须在第一行)：
		// Cannot refer to an instance field startTime while explicitly invoking a constructor
		public TaskWrapper(final String type, final Stat stat, final Callable<V> task) {
			this(type, stat, task, new long[1]);
		}
		private TaskWrapper(final String type, final Stat stat, final Callable<V> task, final long[] startTime) {
			super(new Callable<V>() {
				public V call() throws Exception {
					startTime[0] = System.currentTimeMillis();
					stat.whenJobStart(type);
					return task.call();
				}
			});
			this.type = type;
			this._stat = stat;
			this.startTime = startTime;
		}

		@Override
		protected void done() {
			long start = startTime[0];
			if (start > 0) {
				long cost = System.currentTimeMillis() - start;
				startTime[0] = 0;
				_stat.whenJobStop(type, cost);
			}
		}

		@Override
		public V call() throws Exception {
			throw new UnsupportedOperationException();
		}
	}

	/* ------------------------ constructor ------------------------- */

	public MyExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
			BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.MILLISECONDS, workQueue, threadFactory, handler);
	}

	// public static MyExecutor newInstance(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit{}

	/* ------------------------- singleton ------------------------- */

	static final class SingletonHolder {
		static volatile MyExecutor instance;
		static {
			restart();
		}
		public static void restart() {
			BlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(executorQueueSize);
			ThreadFactory threadFactory = new BasicThreadFactory.Builder().namingPattern("MyExecutor-%d").daemon(true)
			/*.priority(Thread.NORM_PRIORITY)*/.build();
			final MyExecutor oldInstance = instance;
			instance = new MyExecutor(executorCoreThreads, executorMaxThreads, executorKeepAliveMS,
					TimeUnit.MILLISECONDS, queue, threadFactory, new DiscardAndLogPolicy());

			if (oldInstance != null) {
				instance.submit("sys.reset-exec", new Runnable() {
					public void run() {
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
						}
						oldInstance.shutdown();
					}
				});
			}
		}
	}


	/* ------------------------- discard ------------------------- */

	// bell(2012-12): 有discard可以视为世界已经异常了，需有详细的log
	public static class DiscardAndLogPolicy implements RejectedExecutionHandler {

		/* 添加处理逻辑, 如果是内部被依赖的线程(snap_db_gen_pa)则选择执行 */
		public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
			String task;
			if (r instanceof TaskWrapper)
				task = "TaskWrapper(" + ((TaskWrapper<?>) r).type + ")";
			else
				task = String.valueOf(r);
			logger.error("impossible. task " + task + " discarded from " + e.toString());

			long cur = System.currentTimeMillis();
			if (e instanceof MyExecutor && cur > nextPrintTIme)
				printExecStatus((MyExecutor) e, cur);
			//
			rejectedTaskPolicy(r, e);
		}

		// 放开DiscardAndLogPolicy为可继承
		public void rejectedTaskPolicy(Runnable r, ThreadPoolExecutor e) {
			/* 线程池任务提交失败后抛出异常, 以停止线程持续等待到timeout */
			logger.warn("RejectedTaskPolicy from MyExecutor ... ");
			throw new RejectedExecutionException("Task " + r.toString() + " rejected from " + e.toString());
		}

		static final int STAT_REPRINT_TIMEOUT = 10 * 60 * 1000; // 线程池用满时，多久打一次log
		volatile long nextPrintTIme = 0;
		private void printExecStatus(MyExecutor e, long cur) {
			synchronized (this) {
				if (cur > nextPrintTIme)
					nextPrintTIme = cur + STAT_REPRINT_TIMEOUT; // 10min
				else
					return;
			}
			doPrintExeStat(e);
		}
		private void doPrintExeStat(MyExecutor e) {
			Stat stat = e.getStat();
			logger.info("discard debug: exe status:" //
					+ "\n==============================" //
					+ "\n>>>> running:\n" + stat.printRunning() //
					+ "\n>>>> finished:\n" //
					+ stat.printFinished() //
					+ "\n>>>> finished cost:\n" //
					+ stat.printFinishedCosts() //
					+ "\n>>>> avg cost:\n" //
					+ stat.printAverageCosts() //
					+ "\n==============================");
		}
	}


	/* ------------------------- stat ------------------------- */

	public static final class Stat {

		// map<taskName -> [started, fined, cost]>
		final ConcurrentHashMap<String, Number[]> stat = new ConcurrentHashMap<String, Number[]>(20);

		// bell(2012-12): 按目前使用情况估算，只有20多个key
		// bell(2012-12): max(int) = 2147483647，一个1000qps的任务可以保证近25天不会造成计数溢出，足够

		void whenJobStart(String name) {
			Number[] entry = stat.get(name);
			if (entry == null) {
				Number[] newEntry = new Number[] { new AtomicInteger(), new AtomicInteger(), new AtomicLong() };
				Number[] oldEntry = stat.putIfAbsent(name, newEntry);
				entry = oldEntry != null ? oldEntry : newEntry;
			}
			((AtomicInteger) entry[0]).incrementAndGet();
		}

		void whenJobStop(String name, long cost) {
			Number[] entry = stat.get(name);
			if (entry != null) {
				((AtomicInteger) entry[1]).incrementAndGet();
				((AtomicLong) entry[2]).addAndGet(cost);
			}
		}

		public String printRunning() {
			StringBuilder sb = new StringBuilder();
			for (Map.Entry<String, Number[]> entry : stat.entrySet()) {
				int val = entry.getValue()[0].intValue() - entry.getValue()[1].intValue();
				if (val > 0) {
					sb.append(entry.getKey()).append(':').append(val).append('\n');
				}
			}
			return sb.toString().trim();
		}
		public String printFinished() {
			StringBuilder sb = new StringBuilder();
			for (Map.Entry<String, Number[]> entry : stat.entrySet()) {
				long val = entry.getValue()[1].longValue();
				if (val > 0) {
					sb.append(entry.getKey()).append(':').append(val).append('\n');
				}
			}
			return sb.toString().trim();
		}
		public String printFinishedCosts() {
			StringBuilder sb = new StringBuilder();
			for (Map.Entry<String, Number[]> entry : stat.entrySet()) {
				long val = entry.getValue()[2].longValue();
				if (val > 0)
					sb.append(entry.getKey()).append(':').append(val).append("ms\n");
			}
			return sb.toString().trim();
		}
		public String printAverageCosts() {
			StringBuilder sb = new StringBuilder();
			for (Map.Entry<String, Number[]> entry : stat.entrySet()) {
				double val = entry.getValue()[2].doubleValue();
				int count = entry.getValue()[1].intValue();
				if (val > 0 && count > 0) {
					sb.append(entry.getKey()).append(':').append(val / count).append("ms\n");
				}
			}
			return sb.toString().trim();
		}

	}


	/* ------------------------- timer ------------------------- */

	// bell(2013-9): 有些人可能想单独使用timer，所以放成public
	public static final Timer timer = new Timer("executor-timer", true);
	static class WrapperTimerTask extends TimerTask {
		final String name;
		final Runnable task;
		public WrapperTimerTask(String name, Runnable task) {
			this.name = name;
			this.task = task;
		}
		public void run() {
			MyExecutor.instance().submit(name, task);
		}
	}

}
