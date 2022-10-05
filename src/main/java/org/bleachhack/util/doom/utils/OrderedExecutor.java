package org.bleachhack.util.doom.utils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
 
/**
 * An executor that make sure tasks submitted with the same key
 * will be executed in the same order as task submission
 * (order of calling the {@link #submit(Object, Runnable)} method).
 *
 * Tasks submitted will be run in the given {@link Executor}.
 * There is no restriction on how many threads in the given {@link Executor}
 * needs to have (it can be single thread executor as well as a cached thread pool).
 *
 * If there are more than one thread in the given {@link Executor}, tasks
 * submitted with different keys may be executed in parallel, but never
 * for tasks submitted with the same key.
 * 
 * * @param <K> type of keys.
 */
public class OrderedExecutor<K> {
 
    private final Executor executor;
    private final Map<K, Task> tasks;
 
    /**
     * Constructs a {@code OrderedExecutor}.
     *
     * @param executor tasks will be run in this executor.
     */
    public OrderedExecutor(Executor executor) {
        this.executor = executor;
        this.tasks = new HashMap<K, Task>();
    }
 
    /**
     * Adds a new task to run for the given key.
     *
     * @param key the key for applying tasks ordering.
     * @param runnable the task to run.
     */
    public synchronized void submit(K key, Runnable runnable) {
        Task task = tasks.get(key);
        if (task == null) {
            task = new Task();
            tasks.put(key, task);
        }
        task.add(runnable);
    }
 
    /**
     * Private inner class for running tasks for each key.
     * Each key submitted will have one instance of this class.
     */
    private class Task implements Runnable {
 
        private final Lock lock;
        private final Queue<Runnable> queue;
 
        Task() {
            this.lock = new ReentrantLock();
            this.queue = new LinkedList<Runnable>();
        }
 
        public void add(Runnable runnable) {
            boolean runTask;
            lock.lock();
            try {
                // Run only if no job is running.
                runTask = queue.isEmpty();
                queue.offer(runnable);
            } finally {
                lock.unlock();
            }
            if (runTask) {
                executor.execute(this);
            }
        }
 
        @Override
        public void run() {
            // Pick a task to run.
            Runnable runnable;
            lock.lock();
            try {
                runnable = queue.peek();
            } finally {
                lock.unlock();
            }
            try {
                runnable.run();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            // Check to see if there are queued task, if yes, submit for execution.
            lock.lock();
            try {
                queue.poll();
                if (!queue.isEmpty()) {
                    executor.execute(this);
                }
            } finally {
                lock.unlock();
            }
        }
    }
}