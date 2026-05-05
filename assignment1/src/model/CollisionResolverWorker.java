package model;

import util.Latch;

import java.util.List;
import java.util.concurrent.Semaphore;

public class CollisionResolverWorker extends Thread {

    private Latch latch;
    private List<Ball> balls;
    private final int start;
    private final int step;
    private Semaphore semaphore;

    public CollisionResolverWorker(Latch latch, Semaphore semaphore, List<Ball> balls, int start, int step){
        this.latch = latch;
        this.semaphore = semaphore;
        this.balls = balls;
        this.start = start;
        this.step = step;
    }

    public void run(){
        while(true){
            semaphore.tryAcquire();
            for(int i=start; i<balls.size(); i+=step){
                Ball myBall = balls.get(i);
                for(int j=i+1; j<balls.size(); j++){
                    Ball otherBall = balls.get(j);
                    Ball.resolveCollision(myBall,otherBall);
                }
            }
            this.latch.countDown();
        }
    }

}
