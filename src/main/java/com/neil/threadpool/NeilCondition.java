package com.neil.threadpool;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j(topic = "NeilCondition")
public class NeilCondition {

    static ReentrantLock ROOM = new ReentrantLock();
    static boolean hasCigarette = false;
    static boolean hasTakeOut = false;
    static Condition waitCigaretteSet = ROOM.newCondition();
    static Condition waitTakeOutSet = ROOM.newCondition();

    public static void test0(){

        new Thread(()->{

            log.debug("有烟没？[{}]",hasCigarette);
            ROOM.lock();

            try {

                while (!hasCigarette){

                    log.debug("没烟，先歇会");
                    try {
                        waitCigaretteSet.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }

                log.debug("可以开始干活了");

            } finally {
                ROOM.unlock();
            }


        },"小南").start();

        new Thread(()->{

            log.debug("有外卖没？[{}]",hasTakeOut);
            ROOM.lock();

            try {

                while (!hasTakeOut){

                    log.debug("没烟，先歇会");
                    try {
                        waitTakeOutSet.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }

                log.debug("可以开始干活了");

            } finally {
                ROOM.unlock();
            }


        },"小北").start();

        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        new Thread(()->{
            ROOM.lock();
            try {
                waitTakeOutSet.signal();
                hasTakeOut = true;
            }finally {
                ROOM.unlock();
            }
        },"送外卖的").start();

        new Thread(()->{
            ROOM.lock();
            try {
                waitCigaretteSet.signal();
                hasCigarette = true;
            }finally {
                ROOM.unlock();
            }
        },"送烟的").start();

    }

    public static void main(String[] args) {

        NeilCondition.test0();

    }

}
