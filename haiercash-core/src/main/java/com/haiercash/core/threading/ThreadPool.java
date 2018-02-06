package com.haiercash.core.threading;

import org.springframework.core.task.TaskRejectedException;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;

/**
 * Created by 许崇雷 on 2018-01-20.
 */
public final class ThreadPool {
    private static final ThreadPoolTaskExecutor EXECUTOR;
    private static final ThreadPoolTaskScheduler SCHEDULER;

    static {
        EXECUTOR = new ThreadPoolTaskExecutor();
        EXECUTOR.setCorePoolSize(8);
        EXECUTOR.setMaxPoolSize(64);
        EXECUTOR.setQueueCapacity(1024);
        EXECUTOR.setThreadNamePrefix("executor-");
        EXECUTOR.setWaitForTasksToCompleteOnShutdown(true);
        EXECUTOR.setAwaitTerminationSeconds(60);
        EXECUTOR.initialize();
    }

    static {
        SCHEDULER = new ThreadPoolTaskScheduler();
        SCHEDULER.setPoolSize(64);
        SCHEDULER.setThreadNamePrefix("scheduler-");
        SCHEDULER.setWaitForTasksToCompleteOnShutdown(true);
        SCHEDULER.setAwaitTerminationSeconds(60);
        SCHEDULER.initialize();
    }

    public static ThreadPoolTaskExecutor getExecutor() {
        return EXECUTOR;
    }

    public static ThreadPoolTaskScheduler getScheduler() {
        return SCHEDULER;
    }

    /**
     * Submit a Runnable task for execution, receiving a Future representing that task.
     * The Future will return a {@code null} result upon completion.
     *
     * @param task the {@code Runnable} to execute (never {@code null})
     * @return a Future representing pending completion of the task
     * @throws TaskRejectedException if the given task was not accepted
     * @since 3.0
     */
    public static Future<?> submit(Runnable task) {
        return getExecutor().submit(new InheritRunnable(task));
    }

    /**
     * Submit a Callable task for execution, receiving a Future representing that task.
     * The Future will return the Callable's result upon completion.
     *
     * @param task the {@code Callable} to execute (never {@code null})
     * @return a Future representing pending completion of the task
     * @throws TaskRejectedException if the given task was not accepted
     * @since 3.0
     */
    public static <T> Future<T> submit(Callable<T> task) {
        return getExecutor().submit(new InheritCallable<>(task));
    }

    /**
     * Schedule the given {@link Runnable}, invoking it whenever the trigger
     * indicates a next execution time.
     * <p>Execution will end once the scheduler shuts down or the returned
     * {@link ScheduledFuture} gets cancelled.
     *
     * @param task    the Runnable to execute whenever the trigger fires
     * @param trigger an implementation of the {@link Trigger} interface,
     *                e.g. a {@link org.springframework.scheduling.support.CronTrigger} object
     *                wrapping a cron expression
     * @return a {@link ScheduledFuture} representing pending completion of the task,
     * or {@code null} if the given Trigger object never fires (i.e. returns
     * {@code null} from {@link Trigger#nextExecutionTime})
     * @throws org.springframework.core.task.TaskRejectedException if the given task was not accepted
     *                                                             for internal reasons (e.g. a pool overload handling policy or a pool shutdown in progress)
     * @see org.springframework.scheduling.support.CronTrigger
     */
    public static ScheduledFuture<?> schedule(Runnable task, Trigger trigger) {
        return getScheduler().schedule(new InheritRunnable(task), trigger);
    }

    /**
     * Schedule the given {@link Runnable}, invoking it at the specified execution time.
     * <p>Execution will end once the scheduler shuts down or the returned
     * {@link ScheduledFuture} gets cancelled.
     *
     * @param task      the Runnable to execute whenever the trigger fires
     * @param startTime the desired execution time for the task
     *                  (if this is in the past, the task will be executed immediately, i.e. as soon as possible)
     * @return a {@link ScheduledFuture} representing pending completion of the task
     * @throws org.springframework.core.task.TaskRejectedException if the given task was not accepted
     *                                                             for internal reasons (e.g. a pool overload handling policy or a pool shutdown in progress)
     */
    public static ScheduledFuture<?> schedule(Runnable task, Date startTime) {
        return getScheduler().schedule(new InheritRunnable(task), startTime);
    }

    /**
     * Schedule the given {@link Runnable}, invoking it at the specified execution time
     * and subsequently with the given period.
     * <p>Execution will end once the scheduler shuts down or the returned
     * {@link ScheduledFuture} gets cancelled.
     *
     * @param task      the Runnable to execute whenever the trigger fires
     * @param startTime the desired first execution time for the task
     *                  (if this is in the past, the task will be executed immediately, i.e. as soon as possible)
     * @param period    the interval between successive executions of the task (in milliseconds)
     * @return a {@link ScheduledFuture} representing pending completion of the task
     * @throws org.springframework.core.task.TaskRejectedException if the given task was not accepted
     *                                                             for internal reasons (e.g. a pool overload handling policy or a pool shutdown in progress)
     */
    public static ScheduledFuture<?> scheduleAtFixedRate(Runnable task, Date startTime, long period) {
        return getScheduler().scheduleAtFixedRate(new InheritRunnable(task), startTime, period);
    }

    /**
     * Schedule the given {@link Runnable}, starting as soon as possible and
     * invoking it with the given period.
     * <p>Execution will end once the scheduler shuts down or the returned
     * {@link ScheduledFuture} gets cancelled.
     *
     * @param task   the Runnable to execute whenever the trigger fires
     * @param period the interval between successive executions of the task (in milliseconds)
     * @return a {@link ScheduledFuture} representing pending completion of the task
     * @throws org.springframework.core.task.TaskRejectedException if the given task was not accepted
     *                                                             for internal reasons (e.g. a pool overload handling policy or a pool shutdown in progress)
     */
    public static ScheduledFuture<?> scheduleAtFixedRate(Runnable task, long period) {
        return getScheduler().scheduleAtFixedRate(new InheritRunnable(task), period);
    }

    /**
     * Schedule the given {@link Runnable}, invoking it at the specified execution time
     * and subsequently with the given delay between the completion of one execution
     * and the start of the next.
     * <p>Execution will end once the scheduler shuts down or the returned
     * {@link ScheduledFuture} gets cancelled.
     *
     * @param task      the Runnable to execute whenever the trigger fires
     * @param startTime the desired first execution time for the task
     *                  (if this is in the past, the task will be executed immediately, i.e. as soon as possible)
     * @param delay     the delay between the completion of one execution and the start
     *                  of the next (in milliseconds)
     * @return a {@link ScheduledFuture} representing pending completion of the task
     * @throws org.springframework.core.task.TaskRejectedException if the given task was not accepted
     *                                                             for internal reasons (e.g. a pool overload handling policy or a pool shutdown in progress)
     */
    public static ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, Date startTime, long delay) {
        return getScheduler().scheduleWithFixedDelay(new InheritRunnable(task), startTime, delay);
    }

    /**
     * Schedule the given {@link Runnable}, starting as soon as possible and
     * invoking it with the given delay between the completion of one execution
     * and the start of the next.
     * <p>Execution will end once the scheduler shuts down or the returned
     * {@link ScheduledFuture} gets cancelled.
     *
     * @param task  the Runnable to execute whenever the trigger fires
     * @param delay the interval between successive executions of the task (in milliseconds)
     * @return a {@link ScheduledFuture} representing pending completion of the task
     * @throws org.springframework.core.task.TaskRejectedException if the given task was not accepted
     *                                                             for internal reasons (e.g. a pool overload handling policy or a pool shutdown in progress)
     */
    public static ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, long delay) {
        return getScheduler().scheduleWithFixedDelay(new InheritRunnable(task), delay);
    }
}
