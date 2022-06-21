package com.neil.futuretask;

import java.util.concurrent.*;

public class NeilFutureTask {

    public static void main(String[] args) throws ExecutionException, InterruptedException, TimeoutException {

        NeilFutureTask.test2();

    }

    public static void test1()throws ExecutionException, InterruptedException {
        ExecutorService threadPool = Executors.newFixedThreadPool(3);

        long startTime = System.currentTimeMillis();
        FutureTask<String> futureTask1 = new FutureTask<>(() -> {
            try {
                TimeUnit.MILLISECONDS.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            };
            return "task1 over";
        });

        threadPool.submit(futureTask1);

        FutureTask<String> futureTask2 = new FutureTask<>(() -> {
            try {
                TimeUnit.MILLISECONDS.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            };
            return "task2 over";
        });

        threadPool.submit(futureTask2);

        try {
            TimeUnit.MILLISECONDS.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        };
        System.out.println(futureTask1.get());
        System.out.println(futureTask2.get());

        long endTime = System.currentTimeMillis();
        System.out.println("---costTime---"+(endTime-startTime));
        threadPool.shutdown();
    }

    public static void test2() throws ExecutionException, InterruptedException, TimeoutException {

        FutureTask<String> futureTask = new FutureTask<String>(()->{

            System.out.println(Thread.currentThread().getName()+"\t-----come in");
            try{
                TimeUnit.SECONDS.sleep(5);
            }catch (InterruptedException e){
                e.printStackTrace();
            }

            return "task over";

        });

        final Thread thread = new Thread(futureTask);
        thread.start();
        System.out.println(Thread.currentThread().getName()+"\t-----忙完其他任务了");

        // System.out.println(futureTask.get());
        // System.out.println(futureTask.get(3,TimeUnit.SECONDS)); // 超时等待抛出异常

        while (true){

            if (futureTask.isDone()){

                System.out.println(futureTask.get());
                break;

            } else {

                try{
                    TimeUnit.MILLISECONDS.sleep(500);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }

                System.out.println("正在处理中，不要再催了，越催越慢，再催熄火");

            }
        }

    }

}

/**
 * 1、get()容易导致阻塞，一般建议放在程序后面，一旦调用不见不散，非要等到程序执行完成后才会离开，不管计算是否完成，容易程序阻塞。
 * 2、假如我不愿意等待很长时间，我希望过时不候，可以自动离开。
 * 3、isDone()容易导致，轮训的方法回耗费无谓的CPU资源，而且不一定获得想要的结果。
 * Future对于获取计算结果不是很友好。
 */
