package model;

import util.Barrier;

import java.util.List;

public class CollisionWorker extends Thread {

    private Barrier barrier;
    private List<Ball> myBalls;
    private List<Ball> allBalls;

    public CollisionWorker(Barrier barrier, List<Ball> myBalls, List<Ball> allBalls){
        barrier = barrier;
        myBalls = myBalls;
        allBalls = allBalls;
    }

    public void run(){
        try {
            barrier.hitAndWaitAll();

            for(Ball myBall : myBalls){
                for(Ball otherBall : allBalls){
                    if(otherBall == myBall) continue;
                    // TODO: finire
                    Ball.resolveCollision(myBall,otherBall);
                }
            }

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
