package view;

import controller.Controller;
import controller.InputListener;
import model.Board;
import model.BoardObserver;

public class View implements BoardObserver {

	private final ViewFrame frame;
	private final ViewModel viewModel;
	private long lastUpdateTime = System.currentTimeMillis();
	
	public View(ViewModel model, InputListener listener, int w, int h) {
		frame = new ViewFrame(model, listener, w, h);
		frame.setVisible(true);
		viewModel = model;
	}
		
	public void render() {
		frame.render();
	}

	@Override
	public void modelUpdated(Board board) {
		long current = System.currentTimeMillis();
		long elapsed = current - lastUpdateTime;
		lastUpdateTime = current;
		viewModel.update(board, (int) (1000.0/elapsed));
		render();
	}
}
