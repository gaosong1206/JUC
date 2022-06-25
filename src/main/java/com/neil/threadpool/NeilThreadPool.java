package com.neil.threadpool;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

public class NeilThreadPool {

    // 一池5线程
    public static void test0() {

        ExecutorService executorService = Executors.newFixedThreadPool(5);
        try {
            for (int i = 0; i < 10; i++) {
                executorService.execute(() -> {
                    System.out.println(Thread.currentThread().getName() + " 办理业务 ");
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            executorService.shutdown();
        }


    }

    public static void test1() {

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        try {
            for (int i = 0; i < 10; i++) {
                executorService.execute(() -> {
                    System.out.println(Thread.currentThread().getName() + " 办理业务 ");
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            executorService.shutdown();
        }

    }

    // 一池可扩容线程
    public static void test2() {

        ExecutorService executorService = Executors.newCachedThreadPool();
        try {
            for (int i = 0; i < 20; i++) {
                executorService.execute(() -> {
                    System.out.println(Thread.currentThread().getName() + " 办理业务 ");
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            executorService.shutdown();
        }

    }

    /**
     * 创建自定义线程池
     */
    public static void test3() {

        ThreadPoolExecutor executorService = new ThreadPoolExecutor(2,
                5,
                2L,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(3),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy()
        );

        try {
            for (int i = 0; i < 10; i++) {
                executorService.execute(() -> {
                    System.out.println(Thread.currentThread().getName() + " 办理业务 ");
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            executorService.shutdown();
        }

    }

    /**
     * submit方法接收callable对象
     */
    public static void test4() {

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        Future<String> future = executorService.submit(() -> {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "ok";
        });

        try {
            String result = future.get();
            System.out.println(result);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } finally {
            executorService.shutdown();
        }


    }

    public static void test5() {

        ExecutorService executorService = Executors.newFixedThreadPool(3);
        List<Future<String>> list = null;
        try {
            list = executorService.invokeAll(Arrays.asList(
                    () -> {
                        try {
                            TimeUnit.MILLISECONDS.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        return "1";
                    },
                    () -> {
                        try {
                            TimeUnit.MILLISECONDS.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        return "2";
                    },
                    () -> {
                        try {
                            TimeUnit.MILLISECONDS.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        return "3";
                    }
            ));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        list.forEach(f -> {
            try {
                System.out.println(f.get());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        });

        executorService.shutdown();

    }

    public static void test6() {

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        String result = null;
        try {
            result = executorService.invokeAny(Arrays.asList(
                    () -> {
                        try {
                            TimeUnit.MILLISECONDS.sleep(1000);
                        } catch (InterruptedException e) {
                            //e.printStackTrace();
                        }
                        return "1";
                    },
                    () -> {
                        try {
                            TimeUnit.MILLISECONDS.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        return "2";
                    },
                    () -> {
                        try {
                            TimeUnit.MILLISECONDS.sleep(1500);
                        } catch (InterruptedException e) {
                            //e.printStackTrace();
                        }
                        return "1";
                    }
            ));
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        System.out.println(result);
        executorService.shutdown();

    }

    public static void test7() {

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        Future<String> future1 = executorService.submit(() -> {
            System.out.println("task 1 is running");
            try {
                TimeUnit.MILLISECONDS.sleep(1000);
            } catch (InterruptedException e) {
                System.out.println("task 1 is interrupted");
                e.printStackTrace();
            }

            return "1";
        });

        Future<String> future2 = executorService.submit(() -> {
            System.out.println("task 2 is running");
            try {
                TimeUnit.MILLISECONDS.sleep(1000);
            } catch (InterruptedException e) {
                System.out.println("task 2 is interrupted");
                e.printStackTrace();
            }

            return "2";
        });

        Future<String> future3 = executorService.submit(() -> {
            System.out.println("task 3 is running");
            try {
                TimeUnit.MILLISECONDS.sleep(1000);
            } catch (InterruptedException e) {
                System.out.println("task 3 is interrupted");
                e.printStackTrace();
            }

            return "3";
        });

        /*
        executorService.shutdown();
        try {
            executorService.awaitTermination(3,TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("song.gao");
        */
        List<Runnable> runnables = executorService.shutdownNow();
        System.out.println(runnables);

    }

    public static void main(String[] args){
        NeilThreadPool.test7();
    }

}
