package controller;

import model.Board;
import util.V2d;
import java.util.Random;

public class BotAgent extends Thread {
    private final Board board;
    private final Random random = new Random();
    private volatile boolean running = true;

    public BotAgent(Board board) {
        this.board = board;
    }

    @Override
    public void run() {
        while (running) {
            try {
                Thread.sleep(random.nextInt(2000));

                var cpu = board.getCpuBall();
                if (cpu != null && cpu.isActive() && cpu.getVel().abs() < 0.1) {
                    V2d impulse = new V2d(random.nextDouble() - 0.5, random.nextDouble() - 0.5).mul(0.8);
                    board.applyInputToCpu(impulse);
                }
            } catch (InterruptedException e) {
                running = false;
            }
        }
    }

    public void stopBot() {
        this.running = false;
    }
}