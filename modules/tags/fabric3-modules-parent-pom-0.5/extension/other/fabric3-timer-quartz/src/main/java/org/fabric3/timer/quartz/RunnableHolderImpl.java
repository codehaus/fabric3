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

import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;

/**
 * Default implementation of a RunnableHolder.
 *
 * @version $Revision$ $Date$
 */
public class RunnableHolderImpl<T> extends FutureTask<T> implements RunnableHolder<T> {
    private String id;
    private QuartzTimerService timerService;

    public RunnableHolderImpl(String id, Runnable runnable, QuartzTimerService timerService) {
        super(runnable, null);
        this.id = id;
        this.timerService = timerService;
    }

    public String getId() {
        return id;
    }

    public void execute(JobExecutionContext context) throws JobExecutionException {
        boolean result = runAndReset();
        if (!result) {
            try {
                get();
            } catch (ExecutionException e) {
                // unwrap the exception
                JobExecutionException jex = new JobExecutionException(e.getCause());
                jex.setUnscheduleAllTriggers(true);  // unschedule the job
                throw jex;
            } catch (InterruptedException e) {
                JobExecutionException jex = new JobExecutionException(e);
                jex.setUnscheduleAllTriggers(true);  // unschedule the job
                throw jex;
            }
        }
    }

    public boolean cancel(boolean mayInterruptIfRunning) {
        try {
            boolean val = super.cancel(mayInterruptIfRunning);
            // cancel against the timer service
            timerService.cancel(id);
            return val;
        } catch (SchedulerException e) {
            e.printStackTrace(System.err);
            return false;
        }
    }

    public long getDelay(TimeUnit unit) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public int compareTo(Delayed o) {
        throw new UnsupportedOperationException("Not implemented");
    }

}
