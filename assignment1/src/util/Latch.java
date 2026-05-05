package util;

public interface Latch {

    public void await() throws InterruptedException;

    public void countDown();

    public void reset();

}