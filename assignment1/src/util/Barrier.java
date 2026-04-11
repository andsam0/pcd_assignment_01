package util;

public interface Barrier {
    void hitAndWaitAll() throws InterruptedException;
}