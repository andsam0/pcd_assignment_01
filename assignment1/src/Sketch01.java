import controller.BotAgent;
import controller.Controller;
import model.Board;
import view.View;
import view.ViewModel;

public class Sketch01 {

	public static void main(String[] argv) {

		Board model = new Board();
		model.init(new config.LargeBoardConf());
		ViewModel viewModel = new ViewModel();

		Controller controller = new Controller(model);
		View view = new View(viewModel, controller, 1200, 800);
		model.addObserver(view);
		controller.start();

		BotAgent bot = new BotAgent(model);
		bot.start();
	}
	
}
