package com.alibaba.otter.manager.biz.check.thread;

import java.util.concurrent.*;

/**
 * 线程管理
 * @author tangdelong
 * 2017年1月12日
 */
public class ExecutorManager {
	
	private static ExecutorManager executorManager;
	
	private final ExecutorService executor;
	
	/**
	 * 默认数
	 */
	private static int defaultThreadNum = 20;

	private static int maxThreadNum = 20;

	/**
	 * 程序运行时创建一个静态只读的进程辅助对象 
	 */
	private static final Object sysnRoot = new Object();
	
	
	public ExecutorManager(int threadNum){
		executor = new ThreadPoolExecutor(defaultThreadNum, maxThreadNum,
				0L, TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue<Runnable>());
	}
	
	
	public static ExecutorManager getInstance(){
		if(executorManager == null){
			synchronized (sysnRoot){
				if(executorManager == null){
					executorManager = new ExecutorManager(defaultThreadNum);
				}
			}
		}
		return executorManager;
	}
	
	


	/**
	 * 执行线程
	 * @author tangdelong
	 * 2017年1月22日
	 */
	public static void execute(Runnable command){
		ExecutorManager.getInstance().executor.execute(command);
	}
	
	
	/**
	 *  提交线程
	 * @author tangdelong
	 * 2017年1月22日
	 */
	public static <T> Future<T> submit(Callable<T> task){
		return ExecutorManager.getInstance().executor.submit(task);
	}
	
}
