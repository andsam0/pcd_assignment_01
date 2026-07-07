import controller.BotAgent;
import controller.Controller;
import model.Board;
import view.View;
import view.ViewModel;

public class Performance {
    public static void main(String[] argv) {
        Board model = new Board();
        model.init(new config.MassiveBoardConf());

        BotAgent bot = new BotAgent(model);
        bot.start();

        int iterations = 50;
        int i = 0;
        long total = 0;
        long lastUpdateTime = System.currentTimeMillis();
        while (i++ < iterations) {
            long current = System.currentTimeMillis();
            long elapsed = current - lastUpdateTime;
            total += elapsed;
            lastUpdateTime = current;
            model.updateState(elapsed);
        }
        System.out.println(((double) total) / iterations);
        bot.stopBot();
    }
}
