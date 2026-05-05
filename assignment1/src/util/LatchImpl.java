package util;

import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class LatchImpl implements Latch {
    private final Lock mutex = new ReentrantLock();
    private final Condition allPassed;
    private boolean allFinished = false;
    private int nParticipants;
    private int currentParticipants;
    private Semaphore semaphore;

    public LatchImpl(int nParticipants, Semaphore semaphore) {
        this.nParticipants = nParticipants;
        this.semaphore = semaphore;
        this.currentParticipants = nParticipants;
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
            currentParticipants--;
            allFinished = currentParticipants == 0;
            if(allFinished) allPassed.signalAll();
        }finally {
            this.mutex.unlock();
        }
    }

    @Override
    public void reset(){
        currentParticipants = nParticipants;
        semaphore.release(nParticipants);
    }
}
