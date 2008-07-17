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
package org.fabric3.binding.ws.axis2.runtime.config;

import org.apache.axis2.util.threadpool.ThreadFactory;

import org.fabric3.spi.services.work.WorkScheduler;

/**
 * Wrapper to use the Fabric3 work scheduler to handle work from the Axis2 extension.
 *
 * @version $Revision$ $Date$
 */
public class F3ThreadFactory implements ThreadFactory {
    private WorkScheduler scheduler;

    public F3ThreadFactory(WorkScheduler scheduler) {
        this.scheduler = scheduler;
    }

    public void execute(Runnable runnable) {
        scheduler.scheduleWork(runnable);
    }
}
