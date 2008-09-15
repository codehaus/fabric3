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

import org.quartz.Scheduler;
import org.quartz.SchedulerConfigException;
import org.quartz.SchedulerException;
import org.quartz.core.JobRunShell;
import org.quartz.core.JobRunShellFactory;
import org.quartz.core.SchedulingContext;

/**
 * Factory for the standard JobRunShell that wraps job invocations.
 *
 * @version $Revision$ $Date$
 */
public class F3JobRunShellFactory implements JobRunShellFactory {
    private JobRunShell shell;

    public void initialize(Scheduler scheduler, SchedulingContext context) throws SchedulerConfigException {
        shell = new F3JobRunShell(this, scheduler, context);
    }

    public JobRunShell borrowJobRunShell() throws SchedulerException {
        return shell;
    }

    public void returnJobRunShell(JobRunShell jobRunShell) {

    }
}