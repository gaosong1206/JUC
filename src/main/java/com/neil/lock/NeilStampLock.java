package com.neil.lock;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.StampedLock;

@Slf4j(topic = "NeilStampLock")
public class NeilStampLock {

    public static void test0() {

        DataContainerStamped dataContainerStamped = new DataContainerStamped(1);

        new Thread(()->{
            dataContainerStamped.read(1);
        },"t1").start();

        try {
            TimeUnit.SECONDS.sleep((long) 0.5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        /*
        new Thread(()->{
            dataContainerStamped.read(0);
        },"t2").start();
        */

        new Thread(()->{
            dataContainerStamped.write(256);
        },"t2").start();

    }

    public static void main(String[] args) {
        NeilStampLock.test0();
    }


}

@Slf4j(topic = "DataContainerStamped")
class DataContainerStamped {

    private int data;

    private final StampedLock lock = new StampedLock();

    public DataContainerStamped(int data) {
        this.data = data;
    }

    public int read(int readTime) {

        long stamp = lock.tryOptimisticRead();
        log.debug("optimistic read locking... {}", stamp);

        try {
            TimeUnit.SECONDS.sleep(readTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (lock.validate(stamp)) {
            log.debug("read finish... {}", stamp);
            return data;
        }

        // 锁升级，乐观读锁升级到读锁
        log.debug("updating to read lock... {}", stamp);
        try {
            stamp = lock.readLock();
            log.debug("read lock {}", stamp);
            TimeUnit.SECONDS.sleep(readTime);
            log.debug("read finish...... {}", stamp);
            return data;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return -1;
        } finally {
            log.debug("read unlock {}", stamp);
            lock.unlock(stamp);
        }

    }

    public void write(int newData) {

        long stamp = lock.writeLock();
        log.debug("write lock {}", stamp);

        try {

            TimeUnit.SECONDS.sleep(2);
            this.data = newData;

        } catch (InterruptedException e) {

            e.printStackTrace();

        } finally {

            lock.unlock(stamp);

        }


    }

}
