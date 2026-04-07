package controller;

import model.Board;
import util.V2d;
import view.View;
import view.ViewModel;

import java.awt.event.KeyEvent;

public class Controller extends Thread implements InputListener {
    private final Board board;
    private volatile boolean running = true;

    public Controller(Board board) {
        this.board = board;
    }

    @Override
    public void run() {
        long lastUpdateTime = System.currentTimeMillis();
//        long t0 = lastUpdateTime;
//        int nFrames = 0;

        while (running) {
            long current = System.currentTimeMillis();
            long elapsed = current - lastUpdateTime;
            lastUpdateTime = current;

            // 1. Update Physics
            board.updateState(elapsed);

            // 2. Calculate FPS
//            nFrames++;
//            int fps = 0;
//            long totalTime = current - t0;
//            if (totalTime > 0) {
//                fps = (int) (nFrames * 1000 / totalTime);
//            }

            // 3. Sync with View
//            viewModel.update(board, fps);
        }
    }

    @Override
    public void onInputReceived(int keyCode) {
        double intensity = 0.3;
        V2d impulse = switch (keyCode) {
            case KeyEvent.VK_UP    -> new V2d(0, intensity);
            case KeyEvent.VK_DOWN  -> new V2d(0, -intensity);
            case KeyEvent.VK_LEFT  -> new V2d(-intensity, 0);
            case KeyEvent.VK_RIGHT -> new V2d(intensity, 0);
            default -> null;
        };

        if (impulse != null) {
            board.applyInputToPlayer(impulse);
        }
    }
}