/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.fabric3.timer.quartz;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.transaction.TransactionManager;

import junit.framework.TestCase;

import org.fabric3.spi.services.work.NotificationListener;
import org.fabric3.spi.services.work.WorkScheduler;

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

            public <T extends Runnable> void scheduleWork(T work, NotificationListener<T> listener) {
                work.run();
            }

            public <T extends Runnable> void scheduleWork(T work) {
                work.run();
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
