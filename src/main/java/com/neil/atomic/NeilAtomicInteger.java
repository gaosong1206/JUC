package com.neil.atomic;

import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntUnaryOperator;

public class NeilAtomicInteger {

    public static void test0(){

        AtomicInteger atomicInteger = new AtomicInteger(0);
        int result = atomicInteger.incrementAndGet();
        System.out.println(result);

        result = atomicInteger.getAndIncrement();
        System.out.println(result);

        result = atomicInteger.getAndAdd(5);
        System.out.println(result);

        result = atomicInteger.addAndGet(5);
        System.out.println(result);
        System.out.println(atomicInteger.get());


    }

    public static void test1(){

        AtomicInteger atomicInteger = new AtomicInteger(5);

        // lamada表达式代表的是读取到的值，返回值为设置的值。
        int result = atomicInteger.updateAndGet(value-> {

            return value * 10;

        });

        /*
        int result = atomicInteger.getAndUpdate(value-> {

            return value * 10;

        });
        */

        System.out.println(atomicInteger.get());

    }

    public static void test2(AtomicInteger atomicInteger){

        while (true){

            int prev = atomicInteger.get();

            int next =  prev*10;

            if (atomicInteger.compareAndSet(prev,next)){
                break;
            }

        }

    }

    public static void updateAndGet(AtomicInteger atomicInteger, IntUnaryOperator intUnaryOperator){

        while (true){

            int prev = atomicInteger.get();

            int next =  intUnaryOperator.applyAsInt(prev);

            if (atomicInteger.compareAndSet(prev,next)){
                break;
            }

        }

    }

    public static void main(String[] args) {

        AtomicInteger atomicInteger = new AtomicInteger(5);

        NeilAtomicInteger.updateAndGet(atomicInteger,(value)->{
            return value*100;
        });

        System.out.println(atomicInteger.get());

    }

}


