package com.neil.atomic;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

public class NeilAtomicReference {

    public static void main(String[] args) {

        DecimalAccount.demo(new DecimalAccountCas(new BigDecimal("10000")));

    }

}

class DecimalAccountCas implements DecimalAccount{

    private AtomicReference<BigDecimal> balance;

    public DecimalAccountCas(BigDecimal balance){
        this.balance = new AtomicReference<>(balance);
    }

    @Override
    public BigDecimal getBalance() {
        return balance.get();
    }

    @Override
    public void withDraw(BigDecimal account) {

        while (true){

            BigDecimal prev = balance.get();
            BigDecimal next = prev.subtract(account);

            boolean success = balance.compareAndSet(prev, next);
            if (success){
                break;
            }

        }

    }
}


interface DecimalAccount{

    // 获取余额
    BigDecimal getBalance();

    // 取款
    void withDraw(BigDecimal account);

    /**
     * 方法内会启动1000个线程，每个线程-10元操作
     * 如果初始余额为10000，那么正确的结果应当是0
     */
    static void demo(DecimalAccount account){

        ArrayList<Thread> ts = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            ts.add(new Thread(()->{
                account.withDraw(BigDecimal.TEN);
            }));
        }

        ts.forEach(Thread::start);

        ts.forEach(t->{
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        System.out.println(account.getBalance());

    }

}
