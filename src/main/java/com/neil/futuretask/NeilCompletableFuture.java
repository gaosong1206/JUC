package com.neil.futuretask;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class NeilCompletableFuture {

    public static void main(String[] args){

        NeilCompletableFuture.test9();

    }

    public static List<NetMall> list = Arrays.asList(
            new NetMall("jd"),
            new NetMall("dangdang"),
            new NetMall("taobao"),
            new NetMall("pdd"),
            new NetMall("tmall")
    );

    public static List<String> getPrice(List<NetMall> list,String productName){
        return
                list
                .stream()
                .map(netMall -> {
                    return String.format(productName+" in %s price is %.2f",
                            netMall.getNetMallName(),
                            netMall.calcPrice(productName));
                })
                .collect(Collectors.toList());
    };

    public static List<String> getPriceByCompletableFuture(List<NetMall>list,String productName){

        return
                list
                        .stream()
                        .map(netMall -> {
                            CompletableFuture<String> completableFuture = CompletableFuture.supplyAsync(() -> {
                                        return String.format(productName + " in %s price is %.2f",
                                                netMall.getNetMallName(),
                                                netMall.calcPrice(productName));
                                    }
                            );
                            return completableFuture;
                        })
                        .collect(Collectors.toList())
                        .stream()
                        .map(s->s.join())
                        .collect(Collectors.toList());

    }

    public static void test1() throws ExecutionException, InterruptedException {

        final ExecutorService threadPool = Executors.newFixedThreadPool(3);

        CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(() -> {
            System.out.println(Thread.currentThread().getName());
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, threadPool);

        System.out.println(completableFuture.get());

        threadPool.shutdown();
    }

    public static void test2() throws ExecutionException, InterruptedException {

        final ExecutorService threadPool = Executors.newFixedThreadPool(3);

        CompletableFuture<String> completableFuture = CompletableFuture.supplyAsync(() -> {
            System.out.println(Thread.currentThread().getName());
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            ;
            return "hello supplyAsync";
        }/*, threadPool*/);

        System.out.println(completableFuture.get());

        threadPool.shutdown();
    }

    public static void test3() throws ExecutionException, InterruptedException {

        CompletableFuture<Integer> completableFuture = CompletableFuture.supplyAsync(() -> {

            System.out.println(Thread.currentThread().getName() + "---come in");

            final int result = ThreadLocalRandom.current().nextInt(10);
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            ;

            System.out.println("----1秒钟后出结果："+result);

            return result;
        });

        System.out.println(Thread.currentThread().getName() + "线程先去忙其他的任务");

        System.out.println(completableFuture.get());

    }

    public static void test4() throws ExecutionException, InterruptedException {

        ExecutorService threadPool = Executors.newFixedThreadPool(3);

        try{
            CompletableFuture<Integer> completableFuture = CompletableFuture.supplyAsync(() -> {

                System.out.println(Thread.currentThread().getName() + "---come in");

                final int result = ThreadLocalRandom.current().nextInt(10);

                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                ;

                System.out.println("----1秒钟后出结果："+result);

                if (result>5){
                    int i = 1/0;
                }

                return result;

            },threadPool).whenComplete((v,e)->{

                if (e==null){
                    System.out.println("----计算完成，更新系统UpdateValue："+v);
                }

            }).exceptionally(e->{
                e.printStackTrace();
                System.out.println("异常情况："+e.getCause()+"\t"+e.getMessage());
                return null;
            });

            System.out.println(Thread.currentThread().getName() + "线程先去忙其他的任务");
        }catch (Exception e){
            e.printStackTrace();
        }finally{
            threadPool.shutdown();
        }

        // 主线程不要立刻结束，否则CompleteFuture默认使用的线程池会立刻关闭：暂停3秒钟线程
        /*
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/

    }

    public static void test5() {

        CompletableFuture<String> completableFuture = CompletableFuture.supplyAsync(() -> {
            return "hello 1234";
        });

        System.out.println(completableFuture.join());

    }

    public static void test6(){

        long startTime = System.currentTimeMillis();

        List<String> mysqlList = getPrice(list, "mysql");

        for (String element:mysqlList) {
            System.out.println(element);
        }

        long endTime = System.currentTimeMillis();

        System.out.println("-----costTime："+(endTime-startTime)+" 毫秒");

        long startTime1 = System.currentTimeMillis();

        List<String> mysqlList1 = getPriceByCompletableFuture(list, "mysql");

        for (String element:mysqlList) {
            System.out.println(element);
        }

        long endTime1 = System.currentTimeMillis();

        System.out.println("-----costTime："+(endTime1-startTime1)+" 毫秒");

    }

    public static void test7(){

        final CompletableFuture<String> completableFuture = CompletableFuture.supplyAsync(() -> {

            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return "neil";

        });

        //System.out.println(completableFuture.get());
        //System.out.println(completableFuture.get(2L,TimeUnit.SECONDS));

        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(completableFuture.getNow("xxx"));
        System.out.println(completableFuture.complete("completeValue")+"\t"+completableFuture.join());

    }

    public static void test8(){

        final ExecutorService threadPool = Executors.newFixedThreadPool(3);

        CompletableFuture<Integer> completableFuture = CompletableFuture
                .supplyAsync(() -> {

            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("111");
            return 1;

        },threadPool)
                .thenApply(f->{
                    //int i = 1/0;
                    System.out.println("222");
                    return f+2;
                }).thenApply(f -> {
                    System.out.println("333");
                    return f+3;
                }).whenComplete((v,e)->{
                    if (e==null){
                        System.out.println("----计算结果："+v);
                    }
                }).exceptionally(e -> {
                    //e.printStackTrace();
                    System.out.println(e.getMessage());
                    return null;
                });

        System.out.println(Thread.currentThread().getName()+"----主线程先去忙其他任务");
        threadPool.shutdown();

    }

    public static void test9(){

        /*
        final ExecutorService threadPool = Executors.newFixedThreadPool(3);

        CompletableFuture.supplyAsync(()->{
            return 1;
        },threadPool).thenApply(f->{
            return f+2;
        }).thenApply(f->{
            return f+3;
        }).thenAccept(
                //r->{System.out.println(r);}
                System.out::println);

        threadPool.shutdown();
        */

    }



}

@AllArgsConstructor
@NoArgsConstructor
@Data
@Accessors(chain = true)
class Student{
    private Integer id;
    private String studentName;
    private String major;
}

class NetMall{

    @Getter
    private String netMallName;

    public NetMall(String netMallName){

        this.netMallName = netMallName;

    }

    public double calcPrice(String productName){

        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        double result = ThreadLocalRandom.current().nextDouble() * 2 + productName.charAt(0);

        return result;
    };

}
