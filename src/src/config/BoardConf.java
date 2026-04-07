package config;

import model.Ball;
import util.Boundary;

import java.util.List;

public interface BoardConf {

	Boundary getBoardBoundary();
	
	Ball getPlayerBall();
	
	List<Ball> getSmallBalls();

	Ball getCpuBall();

	List<Ball> getHoles();
}
