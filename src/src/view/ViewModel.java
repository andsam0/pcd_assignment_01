package view;

import model.Board;
import util.P2d;
import util.V2d;

import java.util.ArrayList;
import java.util.List;

record BallViewInfo(P2d pos, double radius) {}

public class ViewModel {

	private final List<BallViewInfo> balls;
	private BallViewInfo player;
	private BallViewInfo cpu;
	private final List<BallViewInfo> holes;
	private int framePerSec;

	
	public ViewModel() {
        holes = new ArrayList<BallViewInfo>();;
        balls = new ArrayList<BallViewInfo>();
		framePerSec = 0;
	}
	
	public synchronized void update(Board board, int framePerSec) {
		balls.clear();
		for (var b: board.getBalls()) {
			balls.add(new BallViewInfo(b.getPos(), b.getRadius()));
		}
		this.framePerSec = framePerSec;
		var p = board.getPlayerBall();
		var cpuBall = board.getCpuBall();
		cpu = new BallViewInfo(cpuBall.getPos(), cpuBall.getRadius());
		player = new BallViewInfo(p.getPos(), p.getRadius());
		for(var h: board.getHoles()){
			holes.add(new BallViewInfo(h.getPos(), h.getRadius()));
		}
	}
	
	public synchronized List<BallViewInfo> getBalls(){
        return new ArrayList<>(balls);
	}

	public synchronized int getFramePerSec() {
		return framePerSec;
	}

	public synchronized BallViewInfo getPlayerBall() {
		return player;
	}

	public synchronized BallViewInfo getCpuBall() {
		return cpu;
	}

	public synchronized List<BallViewInfo> getHoles() {
		return new ArrayList<>(holes);
	}

}
