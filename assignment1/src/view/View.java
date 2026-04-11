package view;

import controller.Controller;
import controller.InputListener;
import model.Board;
import model.BoardObserver;

public class View implements BoardObserver {

	private final ViewFrame frame;
	private final ViewModel viewModel;
	
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
		viewModel.update(board, 0);
		render();
	}
}
