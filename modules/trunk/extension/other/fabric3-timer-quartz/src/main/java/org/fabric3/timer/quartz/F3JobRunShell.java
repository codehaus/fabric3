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

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.core.JobRunShell;
import org.quartz.core.JobRunShellFactory;
import org.quartz.core.SchedulingContext;

/**
 * Placeholder shell for monitoring events.
 *
 * @version $Revision$ $Date$
 */
public class F3JobRunShell extends JobRunShell {
    public F3JobRunShell(JobRunShellFactory jobRunShellFactory, Scheduler scheduler, SchedulingContext schedulingContext) {
        super(jobRunShellFactory, scheduler, schedulingContext);
    }

    protected void begin() throws SchedulerException {
    }

    protected void complete(boolean successfull) throws SchedulerException {
    }

}
