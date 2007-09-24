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
package org.fabric3.resource.jndi.proxy.jta;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.fabric3.resource.jndi.proxy.AbstractProxy;

/**
 * Proxy class for a JNDI-based transaction manager.
 * 
 * @version $Revision$ $Date$
 */
public class TransactionManagerProxy extends AbstractProxy<TransactionManager> implements TransactionManager {

    /**
     * @see javax.transaction.TransactionManager#begin()
     */
    public void begin() throws NotSupportedException, SystemException {
        getDelegate().begin();
    }

    /**
     * @see javax.transaction.TransactionManager#commit()
     */
    public void commit() throws HeuristicMixedException, HeuristicRollbackException, IllegalStateException, RollbackException, SecurityException, SystemException {
        getDelegate().commit();
    }

    /**
     * @see javax.transaction.TransactionManager#getStatus()
     */
    public int getStatus() throws SystemException {
        return getDelegate().getStatus();
    }

    /**
     * @see javax.transaction.TransactionManager#getTransaction()
     */
    public Transaction getTransaction() throws SystemException {
        return getDelegate().getTransaction();
    }

    /**
     * @see javax.transaction.TransactionManager#resume(javax.transaction.Transaction)
     */
    public void resume(Transaction transaction) throws IllegalStateException, InvalidTransactionException, SystemException {
        getDelegate().resume(transaction);
    }

    /**
     * @see javax.transaction.TransactionManager#rollback()
     */
    public void rollback() throws IllegalStateException, SecurityException, SystemException {
        getDelegate().rollback();
    }

    /**
     * @see javax.transaction.TransactionManager#setRollbackOnly()
     */
    public void setRollbackOnly() throws IllegalStateException, SystemException {
        getDelegate().setRollbackOnly();
    }

    /**
     * @see javax.transaction.TransactionManager#setTransactionTimeout(int)
     */
    public void setTransactionTimeout(int timeout) throws SystemException {
        getDelegate().setTransactionTimeout(timeout);
    }

    /**
     * @see javax.transaction.TransactionManager#suspend()
     */
    public Transaction suspend() throws SystemException {
        return getDelegate().suspend();
    }

}
