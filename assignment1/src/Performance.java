import controller.BotAgent;
import controller.Controller;
import model.Board;
import view.View;
import view.ViewModel;

public class Performance {
    public static void main(String[] argv) {
        Board model = new Board();
        model.init(new config.MassiveBoardConf());
        int iterations = 2000;
        int i = 0;
        long total = 0;
        long avg = 0;
        long lastUpdateTime = System.currentTimeMillis();
        while (i++ < iterations) {
            long current = System.currentTimeMillis();
            long elapsed = current - lastUpdateTime;
            total += elapsed;
            lastUpdateTime = current;
            model.updateState(elapsed);
            if (i % 100 == 0) {
                avg += total;
                System.out.println("Avg at pass " + i + " : " + ((double) total) / 100);
                total = 0;
            }
        }
        System.out.println("Avg total: " + (double)avg/iterations);
    }

}
