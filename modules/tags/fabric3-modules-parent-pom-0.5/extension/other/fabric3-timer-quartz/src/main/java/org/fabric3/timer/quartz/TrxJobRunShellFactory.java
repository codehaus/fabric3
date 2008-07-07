package org.fabric3.timer.quartz;

import javax.transaction.TransactionManager;

import org.quartz.Scheduler;
import org.quartz.SchedulerConfigException;
import org.quartz.SchedulerException;
import org.quartz.core.JobRunShell;
import org.quartz.core.JobRunShellFactory;
import org.quartz.core.SchedulingContext;

/**
 * Factory for the standard JobRunShell that wraps job invocations in a transaction.
 *
 * @version $Revision$ $Date$
 */
public class TrxJobRunShellFactory implements JobRunShellFactory {
    private TransactionManager tm;
    private TrxJobRunShell shell;

    public TrxJobRunShellFactory(TransactionManager tm) {
        this.tm = tm;
    }

    public void initialize(Scheduler scheduler, SchedulingContext context) throws SchedulerConfigException {
        shell = new TrxJobRunShell(this, scheduler, tm, context);
    }

    public JobRunShell borrowJobRunShell() throws SchedulerException {
        return shell;
    }

    public void returnJobRunShell(JobRunShell jobRunShell) {
        // no-op
    }
}
