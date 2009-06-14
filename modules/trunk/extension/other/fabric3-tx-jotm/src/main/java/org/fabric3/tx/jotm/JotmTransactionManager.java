  /*
   * Fabric3
   * Copyright (c) 2009 Metaform Systems
   *
   * Fabric3 is free software: you can redistribute it and/or modify
   * it under the terms of the GNU General Public License as
   * published by the Free Software Foundation, either version 3 of
   * the License, or (at your option) any later version, with the
   * following exception:
   *
   * Linking this software statically or dynamically with other
   * modules is making a combined work based on this software.
   * Thus, the terms and conditions of the GNU General Public
   * License cover the whole combination.
   *
   * As a special exception, the copyright holders of this software
   * give you permission to link this software with independent
   * modules to produce an executable, regardless of the license
   * terms of these independent modules, and to copy and distribute
   * the resulting executable under terms of your choice, provided
   * that you also meet, for each linked independent module, the
   * terms and conditions of the license of that module. An
   * independent module is a module which is not derived from or
   * based on this software. If you modify this software, you may
   * extend this exception to your version of the software, but
   * you are not obligated to do so. If you do not wish to do so,
   * delete this exception statement from your version.
   *
   * Fabric3 is distributed in the hope that it will be useful,
   * but WITHOUT ANY WARRANTY; without even the implied warranty
   * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
   * See the GNU General Public License for more details.
   *
   * You should have received a copy of the
   * GNU General Public License along with Fabric3.
   * If not, see <http://www.gnu.org/licenses/>.
   */
package org.fabric3.tx.jotm;

import javax.naming.NamingException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.objectweb.jotm.Current;
import org.objectweb.jotm.Jotm;
import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Service;

/**
 * JOTM transaction manager with explicit service interface.
 * 
 * @version $Revision$ $Date$
 */
@Service(javax.transaction.TransactionManager.class)
public final class JotmTransactionManager implements TransactionManager {

    private Current delegate;
    private Jotm jotm;

    /**
     * Initializes JOTM.
     * 
     * @throws NamingException
     */
    @Init
    public void init() throws NamingException {

        this.delegate = Current.getCurrent();

        // If none found, create new local JOTM instance.

        if (this.delegate == null) {
            this.jotm = new Jotm(true, false);
            this.delegate = Current.getCurrent();

        }

    }
    
    /**
     * Stops JOTM.
     */
    @Destroy
    public void destroy() {
        if (this.jotm != null) {
            this.jotm.stop();
        }
    }

    /**
     * @throws SystemException 
     * @throws NotSupportedException 
     * @see javax.transaction.TransactionManager#begin()
     */
    public void begin() throws NotSupportedException, SystemException {
        delegate.begin();
    }

    /**
     * @throws SystemException 
     * @throws RollbackException 
     * @throws HeuristicRollbackException 
     * @throws HeuristicMixedException 
     * @see javax.transaction.TransactionManager#commit()
     */
    public void commit() throws HeuristicMixedException, HeuristicRollbackException, RollbackException, SystemException {
        delegate.commit();
    }

    /**
     * @throws SystemException 
     * @see javax.transaction.TransactionManager#getStatus()
     */
    public int getStatus() throws SystemException {
        return delegate.getStatus();
    }

    /**
     * @throws SystemException 
     * @see javax.transaction.TransactionManager#getTransaction()
     */
    public Transaction getTransaction() throws SystemException {
        return delegate.getTransaction();
    }

    /**
     * @throws SystemException 
     * @throws InvalidTransactionException 
     * @see javax.transaction.TransactionManager#resume(javax.transaction.Transaction)
     */
    public void resume(Transaction transaction) throws InvalidTransactionException, SystemException {
        delegate.resume(transaction);
    }

    /**
     * @throws SystemException  
     * @see javax.transaction.TransactionManager#rollback()
     */
    public void rollback() throws SystemException {
        delegate.rollback();
    }

    /**
     * @throws SystemException 
     * @see javax.transaction.TransactionManager#setRollbackOnly()
     */
    public void setRollbackOnly() throws SystemException {
        delegate.setRollbackOnly();
    }

    /**
     * @throws SystemException 
     * @see javax.transaction.TransactionManager#setTransactionTimeout(int)
     */
    public void setTransactionTimeout(int timeout) throws SystemException {
        delegate.setTransactionTimeout(timeout);
    }

    /**
     * @throws SystemException 
     * @see javax.transaction.TransactionManager#suspend()
     */
    public Transaction suspend() throws SystemException {
        return delegate.suspend();
    }
    
}
