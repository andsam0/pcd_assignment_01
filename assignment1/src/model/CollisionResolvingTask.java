package model;

import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class CollisionResolvingTask implements Runnable {

    private final CyclicBarrier barrier;
    private final List<Ball> balls;
    private final int start;
    private final int step;

    public CollisionResolvingTask(CyclicBarrier barrier, List<Ball> balls, int start, int step) {
        this.barrier = barrier;
        this.balls = balls;
        this.start = start;
        this.step = step;
    }

    public void run() {
        for (int i = start; i < balls.size(); i += step) {
            Ball myBall = balls.get(i);
            for (int j = i + 1; j < balls.size(); j++) {
                Ball otherBall = balls.get(j);
                Ball.resolveCollision(myBall, otherBall);
            }
        }
        try {
            this.barrier.await();
        } catch (InterruptedException | BrokenBarrierException e) {
            throw new RuntimeException(e);
        }
    }

}
