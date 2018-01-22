package com.jarlene.java.utils;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by jarlene on 2017/4/26.
 */
public class ThreadPoolFactoryUtil {

    public static final int HIGH_PRIORITY = 1;
    public static final int NORMAL_PRIORITY = 2;
    public static final int LOW_PRIORITY = 3;

    public static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = CPU_COUNT + 1;
    private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1;
    private static final int KEEP_ALIVE = 15;

    private static final BlockingQueue<Runnable> NORMAL_POOL_WORK_QUEUE =
            new LinkedBlockingQueue<Runnable>(Integer.MAX_VALUE);

    private static final BlockingQueue<Runnable> HIGH_POOL_WORK_QUEUE =
            new LinkedBlockingQueue<Runnable>(128);

    private static final BlockingQueue<Runnable> LOW_POOL_WORK_QUEUE =
            new LinkedBlockingQueue<Runnable>(Integer.MAX_VALUE);

    private static ThreadPoolExecutor NORMAL_EXECUTOR = null;

    private static ThreadPoolExecutor HIGH_EXECUTOR = null;

    private static ThreadPoolExecutor LOW_EXECUTOR = null;

    private static ThreadPoolExecutor create(int priority) {
        ThreadPoolExecutor executor = null;
        switch (priority) {
            case HIGH_PRIORITY:
                executor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE,
                        TimeUnit.SECONDS, HIGH_POOL_WORK_QUEUE, new ExtendedThreadFactory(Thread.MAX_PRIORITY),
                        new ThreadPoolExecutor.DiscardOldestPolicy());
                HIGH_EXECUTOR = executor;
                break;
            case NORMAL_PRIORITY:
                executor =  new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE,
                        TimeUnit.SECONDS, NORMAL_POOL_WORK_QUEUE, new ExtendedThreadFactory(),
                        new ThreadPoolExecutor.DiscardOldestPolicy());
                NORMAL_EXECUTOR = executor;
                break;
            case LOW_PRIORITY:
                executor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE,
                        TimeUnit.SECONDS, LOW_POOL_WORK_QUEUE, new ExtendedThreadFactory(Thread.MIN_PRIORITY),
                        new ThreadPoolExecutor.DiscardOldestPolicy());
                LOW_EXECUTOR = executor;
                break;

        }
        return executor;
    }

    public static ThreadPoolExecutor get() {
        return get(NORMAL_PRIORITY);
    }

    public static ThreadPoolExecutor get(int priority) {
        ThreadPoolExecutor executor = null;
        switch (priority) {
            case HIGH_PRIORITY:
                executor = HIGH_EXECUTOR;
                break;

            case NORMAL_PRIORITY:
                executor = NORMAL_EXECUTOR;
                break;

            case LOW_PRIORITY:
                executor = LOW_EXECUTOR;
                break;

            default:
                throw new IllegalArgumentException("The priority of threadpoolExecutor is error!");
        }
        if (executor == null) {
            synchronized(ThreadPoolFactoryUtil.class) {
                executor = create(priority);
            }
        }
        return executor;
    }

    public static void shutdown() {
        try {
            safeCloseExecutor(LOW_EXECUTOR);
            safeCloseExecutor(NORMAL_EXECUTOR);
            safeCloseExecutor(HIGH_EXECUTOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void safeCloseExecutor(ThreadPoolExecutor executor) {
        if (executor != null) {
            executor.shutdown();
        }
    }


    private static class ExtendedThreadFactory implements ThreadFactory {

        private final AtomicInteger mCount = new AtomicInteger(1);
        private int mPriority = Thread.NORM_PRIORITY;

        public ExtendedThreadFactory() {

        }

        /**
         * 设置线程的优先级,该优先级使用{@link Thread}中定义的优先级范围
         * 需要在Thread.MIN_PRIORITY和Thread.MAX_PRIORITY之间,否则newThread时会抛出异常
         *
         * @param priority 线程优先级
         */
        public ExtendedThreadFactory(int priority) {
            mPriority = priority;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "ThreadPool " + mPriority + "#" + mCount.getAndIncrement());
            t.setPriority(mPriority);
            return t;
        }
    }



}
