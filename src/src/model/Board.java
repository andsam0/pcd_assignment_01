package model;

import config.BoardConf;
import util.Boundary;

import java.util.*;

public class Board {

    private List<Ball> balls;
    private Ball playerBall;
    private Boundary bounds;
    private Ball cpuBall;
    private List<Ball> holes;
    private String winner;
    
    public Board(){} 
    
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
                    it.remove();
                    break;
                }
            }
        }
        // 3. Player/CPU Hole
        for (Ball hole : holes) {
            if (isInHole(playerBall, hole)) {
                playerBall = null;
                return; // Stop processing the frame
            }

            if (isInHole(cpuBall, hole)) {
                cpuBall = null;
                return; // Stop processing the frame
            }
        }
        // 4. Physical Collisions
    	for (int i = 0; i < balls.size() - 1; i++) {
            for (int j = i + 1; j < balls.size(); j++) {
                Ball.resolveCollision(balls.get(i), balls.get(j));
            }
        }
    	for (var b: balls) {
            Ball.resolveCollision(cpuBall, b);
    		Ball.resolveCollision(playerBall, b);
    	}

        Ball.resolveCollision(playerBall, cpuBall);
    	   	    	
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

    private boolean isInHole(Ball b, Ball hole) {
        double dx = b.getPos().x() - hole.getPos().x();
        double dy = b.getPos().y() - hole.getPos().y();
        double distanceSquared = dx * dx + dy * dy;

        // A ball is in the hole if its center enters the hole's radius
        double holeRadius = hole.getRadius();
        return distanceSquared < (holeRadius * holeRadius);
    }
}
