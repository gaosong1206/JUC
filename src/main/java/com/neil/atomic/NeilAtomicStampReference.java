package com.neil.atomic;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicStampedReference;

@Slf4j(topic = "NeilAtomicABA")
public class NeilAtomicStampReference {

    static AtomicReference<String> ref = new AtomicReference<>("A");
    static AtomicStampedReference<String> stampRef = new AtomicStampedReference<>("A", 0);

    public static void main(String[] args) {

        test1();
    }

    private static void test1() {

        log.debug("main start ......");
        String prev = stampRef.getReference();
        int initStamp = stampRef.getStamp();

        log.debug("stamp -> {}",initStamp);

        otherStamp();
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        int lastStamp = stampRef.getStamp();
        log.debug("stamp -> {}",lastStamp);

        log.debug("change A->C {}", stampRef.compareAndSet(prev, "C", initStamp, initStamp + 1));




    }

    private static void test0() {

        log.debug("main start ......");

        // 获取值
        // 这个共享变量被其他线程修改过无法判断出来
        String prev = ref.get();

        other();
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        log.debug("change A->C {}", ref.compareAndSet(prev, "C"));

    }

    private static void other() {

        new Thread(() -> {
            log.debug("change A->B {}", ref.compareAndSet(ref.get(), "B"));
        }).start();

        try {
            TimeUnit.MILLISECONDS.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        new Thread(() -> {
            log.debug("change B->A {}", ref.compareAndSet(ref.get(), "A"));
        }).start();

    }


    private static void otherStamp() {

        new Thread(() -> {

            // 每次都在上一个版本的基础上更新，更新成功版本加1
            int stamp = stampRef.getStamp();
            log.debug("change A->B {}", stampRef.compareAndSet("A", "B", stamp, stamp + 1));

        }).start();


        // 不再是盲区
        log.debug("do something unknown ...... ");
        try {
            TimeUnit.MILLISECONDS.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        new Thread(() -> {

            // 每次都在上一个版本的基础上更新，更新成功版本加1
            int stamp = stampRef.getStamp();
            log.debug("change B->A {}", stampRef.compareAndSet("B", "A", stamp, stamp + 1));


        }).start();

    }

}
