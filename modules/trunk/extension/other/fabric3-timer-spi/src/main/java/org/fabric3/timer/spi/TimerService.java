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
package org.fabric3.timer.spi;

import java.text.ParseException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

/**
 * Provides facilities for execution of tasks at some later time.
 *
 * @version $Revision$ $Date$
 */
public interface TimerService extends ScheduledExecutorService {

    /**
     * Schedules a task for execution according to the cron expression.
     *
     * @param command    the runnable to execute
     * @param expression a valid cron expression
     * @return a future that can be used for synchronization
     * @throws ParseException if an error occurs parsing the cron expression
     * @throws java.util.concurrent.RejectedExecutionException
     *                        if an error occurs scheduling the task
     */
    ScheduledFuture<?> schedule(Runnable command, String expression) throws ParseException;

}
