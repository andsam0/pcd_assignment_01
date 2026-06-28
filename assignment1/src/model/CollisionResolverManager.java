package model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

public class CollisionResolverManager {
    private final List<CollisionResolverWorker> workers = new ArrayList<>();
    private final Semaphore endWorkSem = new Semaphore(0);
    private final int numWorkers;

    public CollisionResolverManager(List<Ball> balls, int numWorkers) {
        this.numWorkers = numWorkers;
        for (int i = 0; i < numWorkers; i++) {
            CollisionResolverWorker w = new CollisionResolverWorker(balls, i, numWorkers, endWorkSem);
            workers.add(w);
            w.start();
        }
    }

    public void startWork() {
        for (var w : workers) w.startWork();
    }

    public void waitForWorkEnd() throws InterruptedException {
        endWorkSem.acquire(numWorkers);
    }

}
