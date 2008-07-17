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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.quartz.Job;
import org.quartz.SchedulerException;
import org.quartz.spi.TriggerFiredBundle;

/**
 * Default implementation of a RunnableJobFactory.
 *
 * @version $Revision$ $Date$
 */
public class RunnableJobFactoryImpl implements RunnableJobFactory {
    private final Map<String, RunnableHolder<?>> runnables;

    public RunnableJobFactoryImpl() {
        runnables = new ConcurrentHashMap<String, RunnableHolder<?>>();
    }

    public Job newJob(TriggerFiredBundle bundle) throws SchedulerException {
        String id = bundle.getJobDetail().getName();
        RunnableHolder<?> runnable = runnables.get(id);
        if (runnable == null) {
            throw new AssertionError("Runnable not found for id: " + id);
        }
        return runnable;
    }

    public void register(RunnableHolder<?> holder) {
        runnables.put(holder.getId(), holder);
    }

    public RunnableHolder<?> remove(String id) {
        return runnables.remove(id);
    }

}
