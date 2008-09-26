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
package org.fabric3.binding.ws.axis2.runtime.config;

import org.apache.axis2.util.threadpool.ThreadFactory;

import org.fabric3.host.work.DefaultPausableWork;
import org.fabric3.host.work.WorkScheduler;

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

    public void execute(final Runnable runnable) {
        scheduler.scheduleWork(new DefaultPausableWork() {
        	public void execute() {
        		runnable.run();
        	}
        });
    }
}
