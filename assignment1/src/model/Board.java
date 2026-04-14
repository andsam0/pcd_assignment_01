package model;

import config.BoardConf;
import util.Barrier;
import util.BarrierImpl;
import util.Boundary;
import util.V2d;

import java.util.*;

public class Board {

    private List<Ball> balls;
    private Ball playerBall;
    private Boundary bounds;
    private Ball cpuBall;
    private List<Ball> holes;
    private final List<BoardObserver> observers = new ArrayList<>();
    private int playerScore = 0;
    private int cpuScore = 0;

    public Board(){}

    public void addObserver(BoardObserver o) {
        observers.add(o);
    }

    private void notifyObservers() {
        for (var o : observers) {
            o.modelUpdated(this);
        }
    }
    public void init(BoardConf conf) {
    	balls = conf.getSmallBalls();    	
    	playerBall = conf.getPlayerBall();
        cpuBall = conf.getCpuBall();
    	bounds = conf.getBoardBoundary();
        holes = conf.getHoles();
    }
    
    public void updateState(long dt) {
        // 1. Standard Movement
    	playerBall.updateState(dt, this);
        cpuBall.updateState(dt, this);
    	for (var b: balls) {
    		b.updateState(dt, this);
    	}
        // 2. Small Balls Hole
        Iterator<Ball> it = balls.iterator();
        while (it.hasNext()) {
            Ball b = it.next();
            for (Ball hole : holes) {
                if (isInHole(b, hole)) {
                    if (b.getLastHitter() == playerBall) {
                        playerScore++;
                    } else if (b.getLastHitter() == cpuBall) {
                        cpuScore++;
                    }
                    it.remove();
                    break;
                }
            }
        }
        // 3. Player/CPU Hole
        for (Ball hole : holes) {
            if (isInHole(playerBall, hole)) {
                playerBall.setActive(false);
                notifyObservers();
                return;
            }
            if (isInHole(cpuBall, hole)) {
                cpuBall.setActive(false);
                notifyObservers();
                return;
            }
        }
        // 4. Physical Collisions

        // TODO: finire
//        int nCores = Runtime.getRuntime().availableProcessors()+1;
//        Barrier barrier = new BarrierImpl(nCores);
//        List<>

    	for (int i = 0; i < balls.size() - 1; i++) {
            for (int j = i + 1; j < balls.size(); j++) {
                Ball.resolveCollision(balls.get(i), balls.get(j));
            }
        }

        for (var b: balls) {
            if (cpuBall != null) Ball.resolveCollision(cpuBall, b);
            if (playerBall != null) Ball.resolveCollision(playerBall, b);
        }
        if (playerBall != null && cpuBall != null) {
            Ball.resolveCollision(playerBall, cpuBall);
        }

        for (int i = 0; i < balls.size() - 1; i++) {
            Ball.applyCollisions(balls.get(i));
        }
        Ball.applyCollisions(playerBall);
        Ball.applyCollisions(cpuBall);

        notifyObservers();
    }
    
    public List<Ball> getBalls(){
    	return balls;
    }
    
    public Ball getPlayerBall() { return playerBall; }

    public Ball getCpuBall() {
        return cpuBall;
    }

    public List<Ball> getHoles() { return holes; }

    public Boundary getBounds(){
        return bounds;
    }

    public int getPlayerScore() { return playerScore; }

    public int getCpuScore() { return cpuScore; }

    private boolean isInHole(Ball b, Ball hole) {
        double dx = b.getPos().x() - hole.getPos().x();
        double dy = b.getPos().y() - hole.getPos().y();
        double distanceSquared = dx * dx + dy * dy;

        // A ball is in the hole if its center enters the hole's radius
        double holeRadius = hole.getRadius();
        return distanceSquared < (holeRadius * holeRadius);
    }

    public synchronized void applyInputToPlayer(V2d impulse) {
        if (this.playerBall != null) {
            this.playerBall.applyKick(impulse);
        }
    }

    public synchronized void applyInputToCpu(V2d impulse) {
        if (this.cpuBall != null && this.cpuBall.isActive()) {
            this.cpuBall.applyKick(impulse);
        }
    }
}
