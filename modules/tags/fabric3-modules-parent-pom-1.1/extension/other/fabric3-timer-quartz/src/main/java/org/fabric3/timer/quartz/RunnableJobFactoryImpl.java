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
