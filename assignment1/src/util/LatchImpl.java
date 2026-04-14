package util;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class LatchImpl implements Latch {
    private final Lock mutex = new ReentrantLock();
    private final Condition allPassed;
    private boolean allFinished = false;
    private int nParticipants;

    public LatchImpl(int nParticipants) {
        this.nParticipants = nParticipants;
        this.allPassed = mutex.newCondition();
    }

    @Override
    public void await() throws InterruptedException {
        try{
            this.mutex.lock();
            while(!allFinished){
                allPassed.await();
            }
        }finally {
            this.mutex.unlock();
        }
    }

    @Override
    public void countDown() {
        try{
            this.mutex.lock();
            nParticipants--;
            allFinished = nParticipants == 0;
            if(allFinished) allPassed.signalAll();
        }finally {
            this.mutex.unlock();
        }
    }
}
