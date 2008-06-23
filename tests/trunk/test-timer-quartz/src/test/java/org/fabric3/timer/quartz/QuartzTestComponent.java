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

import java.util.concurrent.ScheduledFuture;
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

    public void testTransactionalScheduleWithDelay() throws Exception {
        TrxTestRunnable runnable = new TrxTestRunnable();
        ScheduledFuture<?> future = trxTimerService.schedule(runnable, 10, TimeUnit.MILLISECONDS);
        future.get();
        assertTrue(runnable.isInvoked());
        assertTrue(runnable.isTrxStarted());
    }

    public void testNonTransactionalScheduleWithDelay() throws Exception {
        NoTrxTestRunnable runnable = new NoTrxTestRunnable();
        ScheduledFuture<?> future = timerService.schedule(runnable, 10, TimeUnit.MILLISECONDS);
        future.get();
        assertTrue(runnable.isInvoked());
        assertTrue(runnable.isNoTrx());
    }

    private class TrxTestRunnable implements Runnable {
        private boolean invoked;
        private boolean trxStarted;

        public void run() {
            invoked = true;
            try {
                if (tm.getStatus() == Status.STATUS_ACTIVE) {
                    trxStarted = true;
                }
            } catch (SystemException e) {
                // this will cause the test to fail by not setting noTrx
            }
        }

        public boolean isInvoked() {
            return invoked;
        }

        public boolean isTrxStarted() {
            return trxStarted;
        }
    }


    private class NoTrxTestRunnable implements Runnable {
        private boolean invoked;
        private boolean noTrx;

        public void run() {
            invoked = true;
            try {
                if (tm.getStatus() == Status.STATUS_NO_TRANSACTION) {
                    noTrx = true;
                }
            } catch (SystemException e) {
                // this will cause the test to fail
            }
        }

        public boolean isInvoked() {
            return invoked;
        }

        public boolean isNoTrx() {
            return noTrx;
        }
    }

}
