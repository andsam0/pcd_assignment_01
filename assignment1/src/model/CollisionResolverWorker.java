package model;

import util.Latch;

import java.util.List;

public class CollisionResolverWorker extends Thread {

    private Latch latch;
    private List<Ball> myBalls;
    private List<Ball> allBalls;

    public CollisionResolverWorker(Latch latch, List<Ball> myBalls, List<Ball> allBalls){
        this.latch = latch;
        this.myBalls = myBalls;
        this.allBalls = allBalls;
    }

    public void run(){
        for(Ball myBall : myBalls){
            for(Ball otherBall : allBalls){
                if(otherBall == myBall) continue;
                Ball.resolveCollision(myBall,otherBall);
            }
        }
        this.latch.countDown();
    }

}
