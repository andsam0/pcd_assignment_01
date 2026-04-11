package util;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class BarrierImpl implements Barrier {
    private int nParticipants;
    private final Lock mutex = new ReentrantLock();
    private final Condition allFinish;

    public BarrierImpl(int nParticipants) {
        this.nParticipants = nParticipants;
        this.allFinish = mutex.newCondition();
    }

    @Override
    public void hitAndWaitAll() throws InterruptedException {
        try{
            this.mutex.lock();
            nParticipants--;
            while (nParticipants > 0){
                allFinish.await();
            }
            allFinish.signalAll();
        }finally {
            this.mutex.unlock();
        }
    }
}