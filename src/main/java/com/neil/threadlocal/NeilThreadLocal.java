package com.neil.threadlocal;

import java.lang.ref.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class NeilThreadLocal {

    public static void test0() {

        House house = new House();
        for (int i = 0; i < 5; i++) {

            new Thread(() -> {

                int size = new Random().nextInt(5) + 1;
                System.out.println(size);
                for (int j = 0; j <= size; j++) {
                    house.saleHouse();
                }
            }, String.valueOf(i)).start();

        }

        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println(Thread.currentThread().getName() + "\t" + "共计卖出多少套：" + house.saleCount);

    }

    public static void test1() {

        MyData myData = new MyData();
        ExecutorService threadPool = Executors.newFixedThreadPool(3);

        try {

            for (int i = 0; i < 10; i++) {

                threadPool.submit(() -> {

                    try {
                        Integer beforeInt = myData.threadLocalField.get();
                        myData.add();
                        Integer afterInt = myData.threadLocalField.get();
                        System.out.println(Thread.currentThread().getName() + "\t" + "beforeInt:" + beforeInt + "\t" + "afterInt" + afterInt);
                    } finally {
                        myData.threadLocalField.remove();
                    }

                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            threadPool.shutdown();
        }


    }

    /**
     * 强引用
     */
    public static void test2(){

        MyObject myObject = new MyObject();
        System.out.println("gc before : "+myObject);
        myObject = null;

        // 人工开GC，一般不用。
        System.gc();

        System.out.println("gc after : "+myObject);

    }

    /**
     * 软引用，系统内存充足，不回收，内存不够，会回收。
     */
    public static void test3(){

        SoftReference<MyObject> softReference = new SoftReference<>(new MyObject());
        //System.out.println("------softReference:"+softReference.get());

        System.gc();
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("------gc after 内存够用 : "+softReference.get());

        try {
            byte[] bytes = new byte[20 * 1024 * 1024];
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println("------gc after 内存不够用 : "+softReference.get());
        }

    }

    /**
     * 弱引用
     */
    public static void test4(){

        WeakReference<MyObject> weakReference = new WeakReference<>(new MyObject());
        System.out.println("------gc before 内存够用: "+weakReference.get());

        System.gc();
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("------gc after 内存够用: "+weakReference.get());

    }

    /**
     * 虚引用,get方法总是返回null
     */
    public static void test5(){

        MyObject myObject = new MyObject();
        ReferenceQueue<MyObject> referenceQueue = new ReferenceQueue<>();
        PhantomReference<MyObject> phantomReference = new PhantomReference<>(myObject, referenceQueue);

        //System.out.println(phantomReference.get());

        List<byte[]> list = new ArrayList<>();

        new Thread(()->{
            while (true){

                list.add(new byte[1*1024*1024]);

                try {
                    TimeUnit.MILLISECONDS.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(phantomReference.get()+"\t"+"list add ok");
            }
        },"t1").start();

        new Thread(()->{
            while (true){

            Reference<? extends MyObject> reference = referenceQueue.poll();
            if (reference != null){
                System.out.println("有虚对象回收加入了队列");
                break;
            }

            }
        },"t2").start();


    }


    public static void main(String[] args) {

        NeilThreadLocal.test5();

    }

}


class MyObject{

    // 这个方法一般不用复写，我们只是为了教学给大家演示案例做说明
    @Override
    protected void finalize() throws Throwable {

        // finalize的通常目的是在对象不可撤销丢弃之前执行清理操作
        System.out.println("------invoke finalize method------");

    }
}

class MyData {

    ThreadLocal<Integer> threadLocalField = ThreadLocal.withInitial(() -> 0);

    public void add() {
        threadLocalField.set(1 + threadLocalField.get());
    }

}

class House {

    public int saleCount = 0;

    /*
    public ThreadLocal<Integer> saleVolume = new ThreadLocal<Integer>(){

        @Override
        protected Integer initialValue() {
            return 0;
        }

    };*/

    public ThreadLocal<Integer> saleVolume = ThreadLocal.withInitial(() -> {
        return 0;
    });

    public void saleVolumeByThreadLocal() {
        saleVolume.set(1 + saleVolume.get());
    }

    public synchronized void saleHouse() {
        saleCount++;
    }

}
