/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.timer.quartz;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Future;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.List;
import java.util.Collection;

import javax.transaction.TransactionManager;

import junit.framework.TestCase;

import org.fabric3.host.work.DefaultPausableWork;
import org.fabric3.host.work.WorkScheduler;

/**
 * @version $Revision$ $Date$
 */
public class QuartzTimerServiceTestCase extends TestCase {
    private QuartzTimerService timerService;
    private TransactionManager tm;

    public void testNonTransactionalScheduler() throws Exception {
        TestRunnable runnable = new TestRunnable(2);
        timerService.scheduleWithFixedDelay(runnable, 0, 10, TimeUnit.MILLISECONDS);
        runnable.await();
    }

    protected void setUp() throws Exception {
        super.setUp();
        // TODO mock transaction manager
        WorkScheduler workScheduler = new WorkScheduler() {
            public <T extends DefaultPausableWork> void scheduleWork(T work) {
                work.run();
            }

            public void shutdown() {

            }

            public List<Runnable> shutdownNow() {
                return null;
            }

            public boolean isShutdown() {
                return false;
            }

            public boolean isTerminated() {
                return false;
            }

            public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
                return false;
            }

            public <T> Future<T> submit(Callable<T> task) {
                return null;
            }

            public <T> Future<T> submit(Runnable task, T result) {
                return null;
            }

            public Future<?> submit(Runnable task) {
                return null;
            }

            public <T> List<Future<T>> invokeAll(Collection<Callable<T>> tasks) throws InterruptedException {
                return null;
            }

            public <T> List<Future<T>> invokeAll(Collection<Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
                return null;
            }

            public <T> T invokeAny(Collection<Callable<T>> tasks) throws InterruptedException, ExecutionException {
                return null;
            }

            public <T> T invokeAny(Collection<Callable<T>> tasks, long timeout, TimeUnit unit)
                    throws InterruptedException, ExecutionException, TimeoutException {
                return null;
            }

            public void execute(Runnable command) {

            }
        };
        timerService = new QuartzTimerService(workScheduler, tm);
        timerService.setTransactional(false);
        timerService.init();
    }


    private class TestRunnable implements Runnable {
        private CountDownLatch latch;

        private TestRunnable(int num) {
            latch = new CountDownLatch(num);
        }

        public void run() {
            latch.countDown();
        }

        public void await() throws InterruptedException {
            latch.await();
        }

    }

}
