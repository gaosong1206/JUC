package com.neil.threadpool;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j(topic = "NeilTestPool")
public class NeilTestPool {

    public static void test0() {

        ThreadPool threadPool = new ThreadPool(1, 1000, TimeUnit.MILLISECONDS, 1,(queue,task)->{

            // 1、死等
            // queue.put(task);
            // 2、带超时的等待
            // queue.offer(task,1500,TimeUnit.MILLISECONDS);
            // 3、调用者线程放弃任务
            // log.debug("放弃任务{}",task);
            // 4、抛出异常
            // throw new RuntimeException("任务执行失败"+task);
            // 5、调用者自己执行任务
            task.run();


        });
        for (int i = 0; i < 5; i++) {

            int j = i;
            threadPool.execute(() -> {

                try {
                    TimeUnit.MILLISECONDS.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                log.debug("任务{}执行完成", j);
            });

        }


    }

    public static void main(String[] args) {
        NeilTestPool.test0();
    }

}

@Slf4j(topic = "NeilTestPool.ThreadPool")
class ThreadPool {

    // 任务队列
    private NeilBlockingQueue<Runnable> taskQueue;

    // 线程集合
    private HashSet<Worker> workers = new HashSet<>();

    // 核心线程数
    private int coreSize;

    // 获取任务的超时时间
    private long timeout;

    // 时间单位
    private TimeUnit timeUnit;

    // 拒绝策略
    private RejectPolicy<Runnable> rejectPolicy;

    public ThreadPool(int coreSize, long timeout, TimeUnit timeUnit, int queueCapacity,RejectPolicy<Runnable>rejectPolicy) {
        this.taskQueue = new NeilBlockingQueue<>(queueCapacity);
        this.coreSize = coreSize;
        this.timeout = timeout;
        this.timeUnit = timeUnit;
        this.rejectPolicy = rejectPolicy;
    }

    public void execute(Runnable task) {

        // 当任务数没有超过coreSize时，直接交给worker执行
        synchronized (workers) {

            if (workers.size() < coreSize) {

                Worker worker = new Worker(task);
                log.debug("新增worker{},{}", worker, task);
                workers.add(worker);
                worker.start();

            } else {

                // taskQueue.put(task);
                taskQueue.tryPut(rejectPolicy,task);

            }
        }

    }

    class Worker extends Thread {

        private Runnable task;

        public Worker(Runnable task) {
            this.task = task;
        }

        @Override
        public void run() {

            // 执行任务
            // 1、当task不为空，执行任务
            // 2、当task执行完毕，再接着从任务队列获取任务并执行

            //while (task != null || (task = taskQueue.take()) != null){
            while (task != null || (task = taskQueue.poll(timeout, timeUnit)) != null) {

                try {
                    log.debug("正在执行...{}", task);
                    task.run();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    task = null;
                }

            }

            synchronized (workers) {
                log.debug("worker 被移除{}", this);
                workers.remove(this);
            }

        }
    }


}

@FunctionalInterface
interface RejectPolicy<T>{

    void reject(NeilBlockingQueue<T> queue,T task);

}

@Slf4j(topic = "NeilTestPool.NeilBlockingQueue")
class NeilBlockingQueue<T> {

    // 1、任务队列
    private Deque<T> queue = new ArrayDeque<>();

    // 2、锁
    private ReentrantLock lock = new ReentrantLock();

    // 3、生产者条件变量
    private Condition fullWaitSet = lock.newCondition();

    // 4、消费者条件变量
    private Condition emptyWaitSet = lock.newCondition();

    // 5、容量
    private int capacity;

    public NeilBlockingQueue(int capacity) {
        this.capacity = capacity;
    }

    // 带超时的阻塞获取
    public T poll(long timeout, TimeUnit unit) {

        lock.lock();

        try {

            // 将timeout统一转换为纳秒
            long nanos = unit.toNanos(timeout);

            while (queue.isEmpty()) {

                try {

                    // 存在虚假唤醒问题
                    // 循环条件再次进入时，需要等待的时间为剩余的时间
                    if (nanos <= 0) {
                        return null;
                    }

                    // 返回的是剩余的时间
                    nanos = emptyWaitSet.awaitNanos(nanos);


                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            T t = queue.removeFirst();
            fullWaitSet.signal();
            return t;
        } finally {
            lock.unlock();
        }

    }

    // 阻塞获取
    public T take() {

        lock.lock();

        try {
            while (queue.isEmpty()) {
                try {
                    emptyWaitSet.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            T t = queue.removeFirst();
            fullWaitSet.signal();
            return t;
        } finally {
            lock.unlock();
        }

    }

    // 阻塞添加
    public void put(T element) {

        lock.lock();

        try {

            while (queue.size() == capacity) {

                try {
                    log.debug("等待加入任务队列{}", element);
                    fullWaitSet.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }

            log.debug("加入任务队列{}", element);
            queue.addLast(element);
            emptyWaitSet.signal();

        } finally {
            lock.unlock();
        }

    }

    // 带超时时间的阻塞添加
    public boolean offer(T task,long timeout,TimeUnit timeUnit){

        lock.lock();

        try {
            // 将timeout统一转换为纳秒
            long nanos = timeUnit.toNanos(timeout);

            while (queue.size() == capacity) {

                // 存在虚假唤醒问题
                // 循环条件再次进入时，需要等待的时间为剩余的时间
                if (nanos <= 0) {
                    log.debug("等待加入任务队列超过时间{}", task);
                    return false;
                }

                try {

                    log.debug("等待加入任务队列{}", task);

                    // 返回的是剩余的时间
                    nanos = fullWaitSet.awaitNanos(nanos);

                } catch (InterruptedException e) {

                    e.printStackTrace();

                }

            }

            log.debug("加入任务队列{}", task);
            queue.addLast(task);
            emptyWaitSet.signal();
            return true;

        } finally {
            lock.unlock();
        }
    }

    // 获取大小
    public int size() {

        lock.lock();

        try {
            return queue.size();
        } finally {
            lock.unlock();
        }

    }


    public void tryPut(RejectPolicy<T> rejectPolicy, T task) {

        lock.lock();

        try {

            if (queue.size()==capacity){

                rejectPolicy.reject(this,task);

            } else {

                log.debug("等待加入任务队列{}", task);
                queue.addLast(task);
                emptyWaitSet.signal();

            }

        } finally {
            lock.unlock();
        }

    }
}
