package view;

import controller.InputListener;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.*;

public class ViewFrame extends JFrame {
    
    private VisualiserPanel panel;
    private ViewModel model;
    private RenderSynch sync;
	private boolean gameEnded = false;
	private InputListener listener;

    public ViewFrame(ViewModel model, InputListener listener,  int w, int h){
    	this.model = model;
    	this.sync = new RenderSynch();
		this.listener = listener;

    	setTitle("Assigment 1");
        setSize(w,h + 25);
        setResizable(false);

		this.setFocusable(true);
		this.requestFocusInWindow();

		panel = new VisualiserPanel(w,h);
        getContentPane().add(panel);

		this.addKeyListener(new java.awt.event.KeyAdapter() {
			@Override
			public void keyPressed(java.awt.event.KeyEvent e) {
				listener.onInputReceived(e.getKeyCode());
			}
		});

        addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent ev){
				System.exit(-1);
			}
			public void windowClosed(WindowEvent ev){
				System.exit(-1);
			}
		});
    }
     
    public void render(){
		if (gameEnded) return;
		long nf = sync.nextFrameToRender();
        panel.repaint();
		checkWinner();
		try {
			sync.waitForFrameRendered(nf);
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}
    }
	private void checkWinner() {
		// We check if the balls are null (which happens in your Board logic)
		var pb = model.getPlayerBall();
		var cb = model.getCpuBall();

		if (pb == null) {
			showGameOverPopup("CPU WINS! The Player fell into a hole.");
		} else if (cb == null) {
			showGameOverPopup("PLAYER WINS! The CPU fell into a hole.");
		}
	}

	private void showGameOverPopup(String message) {
		gameEnded = true;
		SwingUtilities.invokeLater(() -> {
			JOptionPane.showMessageDialog(this, message, "Game Over", JOptionPane.INFORMATION_MESSAGE);
			System.exit(0); // This stops the entire application
		});
	}

    public class VisualiserPanel extends JPanel {
        private int ox;
        private int oy;
        private int delta;
        
        public VisualiserPanel(int w, int h){
            setSize(w,h + 25);
            ox = w/2;
            oy = h/2;
            delta = Math.min(ox, oy);
        }

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g;

			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

			// Clear background
			g2.setColor(Color.WHITE);
			g2.fillRect(0, 0, getWidth(), getHeight());

			// Draw axis lines
			g2.setColor(Color.LIGHT_GRAY);
			g2.setStroke(new BasicStroke(1));
			g2.drawLine(ox, 0, ox, oy * 2);
			g2.drawLine(0, oy, ox * 2, oy);

			// 1. DRAW HOLES
			g2.setColor(Color.BLACK);
			for (var h : model.getHoles()) {
				fillBall(g2, h);
			}

			// 2. DRAW POINTS
			g2.setColor(Color.BLUE);
			g2.setFont(new Font("Arial", Font.PLAIN, 80));
			g2.drawString(String.valueOf(model.getPlayerScore()), ox - (int)(delta * 0.8), oy + (int)(delta * 0.4));
			g2.drawString(String.valueOf(model.getCpuScore()), ox + (int)(delta * 0.6), oy + (int)(delta * 0.4));

			// 3. DRAW SMALL BALLS
			g2.setColor(Color.BLACK);
			g2.setStroke(new BasicStroke(1));
			for (var b : model.getBalls()) {
				drawBall(g2, b);
			}

			// 4. DRAW PLAYER BALLS
			g2.setStroke(new BasicStroke(2));
			drawBallWithLabel(g2, model.getPlayerBall(), "P");
			drawBallWithLabel(g2, model.getCpuBall(), "C");

			// Debug Info
			g2.setFont(new Font("Arial", Font.PLAIN, 12));
			g2.setColor(Color.BLACK);
			g2.drawString("Num small balls: " + model.getBalls().size(), 20, 150);
			g2.drawString("Frame per sec: " + model.getFramePerSec(), 20, 170);

			sync.notifyFrameRendered();
		}

		// Helper to draw the outline
		private void drawBall(Graphics2D g2, BallViewInfo ball) {
			if (ball != null) {
				int[] coords = getBallCoords(ball);
				g2.drawOval(coords[0], coords[1], coords[2], coords[3]);
			}
		}

		private void fillBall(Graphics2D g2, BallViewInfo ball) {
			if (ball != null) {
				int[] coords = getBallCoords(ball);
				g2.fillOval(coords[0], coords[1], coords[2], coords[3]);
			}
		}

		private void drawBallWithLabel(Graphics2D g2, BallViewInfo ball, String label) {
			if (ball != null) {
				int[] coords = getBallCoords(ball);
				g2.drawOval(coords[0], coords[1], coords[2], coords[3]);

				g2.setFont(new Font("Arial", Font.BOLD, 15));
				FontMetrics fm = g2.getFontMetrics();
				int tx = coords[0] + (coords[2] - fm.stringWidth(label)) / 2;
				int ty = coords[1] + ((coords[3] - fm.getHeight()) / 2) + fm.getAscent();
				g2.drawString(label, tx, ty);
			}
		}

		private int[] getBallCoords(BallViewInfo ball) {
			var p = ball.pos();
			int r = (int) (ball.radius() * delta);
			int x = (int) (ox + p.x() * delta) - r;
			int y = (int) (oy - p.y() * delta) - r;
			return new int[]{x, y, r * 2, r * 2};
		}
	}
}

