/*
 * Fabric3
 * Copyright � 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the �License�), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an �as is� basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.tx.interceptor;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.invocation.Message;
import org.fabric3.tx.TxException;

/**
 * @version $Revision$ $Date$
 */
public class TxInterceptor implements Interceptor {

    private Interceptor next;
    private TransactionManager transactionManager;
    private TxAction txAction;
    private TxMonitor monitor;

    /**
     * Initializes the transaction manager.
     *
     * @param transactionManager Transaction manager to be initialized.
     * @param txAction Transaction action.
     * @param monitor Transaction monitor.
     */
    public TxInterceptor(TransactionManager transactionManager, TxAction txAction, TxMonitor monitor) {
        this.transactionManager = transactionManager;
        this.txAction = txAction;
        this.monitor = monitor;
        monitor.interceptorInitialized(txAction);
    }

    public Interceptor getNext() {
        return next;
    }

    public void setNext(Interceptor next) {
        this.next = next;
    }

    public Message invoke(Message message) {

        Transaction transaction =  getTransaction();

        if (txAction == TxAction.BEGIN) {
            if (transaction == null) {
                begin();
            } else {
                monitor.joined(hashCode());
            }
        } else if (txAction == TxAction.SUSPEND && transaction != null) {
            suspend();
        }

        Message ret;
        try {
            ret = next.invoke(message);
        } catch (RuntimeException e) {
            if(txAction == TxAction.BEGIN && transaction == null) {
                rollback();
            } else if(txAction == TxAction.SUSPEND && transaction != null) {
                setRollbackOnly();
            }
            throw e;
        }

        if(txAction == TxAction.BEGIN && transaction == null && !ret.isFault()) {
            commit();
        } else if(txAction == TxAction.BEGIN && transaction == null && ret.isFault()) {
            rollback();
        } else if(txAction == TxAction.SUSPEND && transaction != null) {
            resume(transaction);
        }

        return ret;

    }

    private void setRollbackOnly() {
        try {
            monitor.markedForRollback(hashCode());
            transactionManager.setRollbackOnly();
        } catch (SystemException e) {
            throw new TxException(e);
        }
    }

    private Transaction getTransaction() {
        try {
            return transactionManager.getTransaction();
        } catch (SystemException e) {
            throw new TxException(e);
        }
    }

    private void rollback() {
        try {
            monitor.rolledback(hashCode());
            transactionManager.rollback();
        } catch (SystemException e) {
            throw new TxException(e);
        }
    }

    private void begin() {
        try {
            monitor.started(hashCode());
            transactionManager.begin();
        } catch (NotSupportedException e) {
            throw new TxException(e);
        } catch (SystemException e) {
            throw new TxException(e);
        }
    }

    private void suspend() {
        try {
            monitor.suspended(hashCode());
            transactionManager.suspend();
        } catch (SystemException e) {
            throw new TxException(e);
        }
    }

    private void resume(Transaction transaction) {
        try {
            monitor.resumed(hashCode());
            transactionManager.resume(transaction);
        } catch (SystemException e) {
            throw new TxException(e);
        } catch (InvalidTransactionException e) {
            throw new TxException(e);
        } catch (IllegalStateException e) {
            throw new TxException(e);
        }
    }

    private void commit() {
        try {
            if (transactionManager.getStatus() != Status.STATUS_MARKED_ROLLBACK) {
                monitor.committed(hashCode());
                transactionManager.commit();
            } else {
                rollback();
            }
        } catch (SystemException e) {
            throw new TxException(e);
        } catch (IllegalStateException e) {
            throw new TxException(e);
        } catch (SecurityException e) {
            throw new TxException(e);
        } catch (HeuristicMixedException e) {
            throw new TxException(e);
        } catch (HeuristicRollbackException e) {
            throw new TxException(e);
        } catch (RollbackException e) {
            throw new TxException(e);
        }
    }

}
