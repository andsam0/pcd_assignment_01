package model;

import util.P2d;
import util.V2d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CollisionMonitor {

    private final List<V2d> velocityDisplacements = new ArrayList<>();
    private final List<P2d> positionDisplacements = new ArrayList<>();
    private final Map<Ball, List<Ball>> lastHitters = new HashMap<>();

    public synchronized void addVelocity(V2d velocity) {
        this.velocityDisplacements.add(velocity);
    }

    public synchronized void addPosition(P2d position) {
        this.positionDisplacements.add(position);
    }

    public synchronized void clear() {
        this.velocityDisplacements.clear();
        this.positionDisplacements.clear();
        this.lastHitters.clear();
    }

    public synchronized List<V2d> getVelocityDisplacements() {
        return this.velocityDisplacements;
    }

    public synchronized List<P2d> getPositionDisplacements() {
        return this.positionDisplacements;
    }

    public synchronized void addLastHitter(Ball hit, Ball hitter) {
        this.lastHitters.putIfAbsent(hit,new ArrayList<>());
        this.lastHitters.get(hit).add(hitter);
    }

    public synchronized Ball getLastHitter(Ball ball) {
        return this.lastHitters.get(ball).getLast();
    }


}
