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
import java.util.concurrent.TimeUnit;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

import junit.framework.TestCase;

import org.fabric3.api.annotation.Resource;
import org.fabric3.timer.spi.TimerService;

/**
 * @version $Revision$ $Date$
 */
public class QuartzTestComponent extends TestCase {

    @Resource(mappedName = "TransactionalTimerService")
    protected TimerService trxTimerService;
    @Resource(mappedName = "NonTransactionalTimerService")
    protected TimerService timerService;

    @Resource(mappedName = "TransactionManager")
    protected TransactionManager tm;

    public void testTransactionalScheduleInterval() throws Exception {
        TrxTestRunnable runnable = new TrxTestRunnable(2);  // test multiple firings
        trxTimerService.scheduleWithFixedDelay(runnable, 0, 10, TimeUnit.MILLISECONDS);
        runnable.await();
        assertTrue(runnable.isTrxStarted());
    }

    public void testNonTransactionalScheduleInterval() throws Exception {
        NoTrxTestRunnable runnable = new NoTrxTestRunnable(2);  // test multiple firings
        timerService.scheduleWithFixedDelay(runnable, 0, 10, TimeUnit.MILLISECONDS);
        runnable.await();
        assertTrue(runnable.isNoTrx());
    }

    public void testTransactionalScheduleWithDelay() throws Exception {
        TrxTestRunnable runnable = new TrxTestRunnable(1); // fires once
        trxTimerService.schedule(runnable, 10, TimeUnit.MILLISECONDS);
        runnable.await();
        assertTrue(runnable.isTrxStarted());
    }

    public void testNonTransactionalScheduleWithDelay() throws Exception {
        NoTrxTestRunnable runnable = new NoTrxTestRunnable(1);   // fires once
        timerService.schedule(runnable, 10, TimeUnit.MILLISECONDS);
        runnable.await();
        assertTrue(runnable.isNoTrx());
    }

    private class TrxTestRunnable implements Runnable {
        private CountDownLatch latch;
        private boolean trxStarted;

        private TrxTestRunnable(int num) {
            latch = new CountDownLatch(num);
        }

        public void run() {
            try {
                latch.countDown();
                if (tm.getStatus() == Status.STATUS_ACTIVE) {
                    trxStarted = true;
                }
            } catch (SystemException e) {
                // this will cause the test to fail by not setting noTrx
            }
        }

        public boolean isTrxStarted() {
            return trxStarted;
        }

        public void await() throws InterruptedException {
            latch.await();
        }
    }


    private class NoTrxTestRunnable implements Runnable {
        private CountDownLatch latch;
        private boolean noTrx;

        private NoTrxTestRunnable(int num) {
            latch = new CountDownLatch(num);
        }

        public void run() {
            try {
                latch.countDown();
                if (tm.getStatus() == Status.STATUS_NO_TRANSACTION) {
                    noTrx = true;
                }
            } catch (SystemException e) {
                // this will cause the test to fail
            }
        }

        public boolean isNoTrx() {
            return noTrx;
        }

        public void await() throws InterruptedException {
            latch.await();
        }
    }

}
