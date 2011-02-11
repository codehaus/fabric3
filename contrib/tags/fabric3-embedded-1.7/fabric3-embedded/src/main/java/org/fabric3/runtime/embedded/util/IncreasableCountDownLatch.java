package org.fabric3.runtime.embedded.util;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Michal Capo
 */
public class IncreasableCountDownLatch {

    private AtomicInteger count;

    public IncreasableCountDownLatch(int initCount) {
        count = new AtomicInteger(initCount);
    }

    public void increase() {
        count.getAndIncrement();
    }

    public void countDown() {
        count.getAndDecrement();
    }

    public void reset() {
        count.set(0);
    }

    public void await() {
        while (0 != count.get()) {
            try {
                Thread.sleep(50L);
            } catch (InterruptedException ignored) {
            }
        }

        try {
            Thread.sleep(1000L);
        } catch (InterruptedException ignored) {
        }
    }

}
