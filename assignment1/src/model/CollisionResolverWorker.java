package model;

import java.util.List;
import java.util.concurrent.Semaphore;

public class CollisionResolverWorker extends Thread {
    private final List<Ball> balls;
    private final int start, step;
    private final Semaphore startWorkSem = new Semaphore(0);
    private final Semaphore endWorkSem;

    public CollisionResolverWorker(List<Ball> balls, int start, int step, Semaphore endWorkSem) {
        this.balls = balls;
        this.start = start;
        this.step = step;
        this.endWorkSem = endWorkSem;
    }

    public void startWork() {
        startWorkSem.release();
    }

    @Override
    public void run() {
        while (true) {
            try {
                startWorkSem.acquire();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            for (int i = start; i < balls.size(); i += step) {
                Ball myBall = balls.get(i);
                for (int j = i + 1; j < balls.size(); j++) {
                    Ball.resolveCollision(myBall, balls.get(j));
                }
            }
            endWorkSem.release();
        }
    }
}
