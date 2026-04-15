package model;

import util.Latch;

import java.util.List;

public class CollisionResolvingTask implements Runnable {

    private Latch latch;
    private List<Ball> balls;
    private final int start;
    private final int step;

    public CollisionResolvingTask(Latch latch, List<Ball> balls, int start, int step){
        this.latch = latch;
        this.balls = balls;
        this.start = start;
        this.step = step;
    }

    public void run(){
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
