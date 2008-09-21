package org.fabric3.tests.function.headers;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @version $Revision$ $Date$
 */
public class HeaderFuture implements Future<AssertionError> {
    private boolean done;
    private CountDownLatch latch = new CountDownLatch(1);
    private AssertionError testError;

    public boolean cancel(boolean mayInterruptIfRunning) {
        throw new UnsupportedOperationException();
    }

    public boolean isCancelled() {
        return false;
    }

    public boolean isDone() {
        return done;
    }

    public AssertionError get() throws InterruptedException, ExecutionException {
        latch.await();
        return testError;
    }

    public AssertionError get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        latch.await(timeout, unit);
        return testError;
    }

    public void complete() {
        latch.countDown();
        done = true;
    }

    public void completeWithError(AssertionError error) {
        testError = error;
        latch.countDown();
        done = true;
    }

}
