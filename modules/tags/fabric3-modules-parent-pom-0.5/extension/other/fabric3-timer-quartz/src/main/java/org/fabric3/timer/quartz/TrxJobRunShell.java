package org.fabric3.timer.quartz;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.core.JobRunShellFactory;
import org.quartz.core.SchedulingContext;

/**
 * JobRunShell that wraps job invocations in a transaction.
 *
 * @version $Revision$ $Date$
 */
public class TrxJobRunShell extends F3JobRunShell {
    private TransactionManager tm;

    public TrxJobRunShell(JobRunShellFactory shellFactory, Scheduler scheduler, TransactionManager tm, SchedulingContext context) {
        super(shellFactory, scheduler, context);
        this.tm = tm;
    }

    protected void begin() throws SchedulerException {
        beginTransaction();
        super.begin();
    }

    protected void complete(boolean successfull) throws SchedulerException {
        super.complete(successfull);
        if (successfull) {
            commitTransaction();
        } else {
            rollbackTransaction();
        }
    }

    private void beginTransaction() throws SchedulerException {
        try {
            tm.begin();
        } catch (NotSupportedException e) {
            throw new SchedulerException(e);
        } catch (SystemException e) {
            throw new SchedulerException(e);
        }
    }

    private void commitTransaction() throws SchedulerException {
        try {
            if (tm.getStatus() != Status.STATUS_MARKED_ROLLBACK) {
                tm.commit();
            } else {
                tm.rollback();
            }
        } catch (SystemException e) {
            throw new SchedulerException(e);
        } catch (IllegalStateException e) {
            throw new SchedulerException(e);
        } catch (SecurityException e) {
            throw new SchedulerException(e);
        } catch (HeuristicMixedException e) {
            throw new SchedulerException(e);
        } catch (HeuristicRollbackException e) {
            throw new SchedulerException(e);
        } catch (RollbackException e) {
            throw new SchedulerException(e);
        }
    }

    private void rollbackTransaction() throws SchedulerException {
        try {
            tm.rollback();
        } catch (SystemException e) {
            throw new SchedulerException(e);
        }
    }

}
