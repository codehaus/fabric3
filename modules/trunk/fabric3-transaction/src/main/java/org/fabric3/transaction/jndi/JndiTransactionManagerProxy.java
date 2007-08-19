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
package org.fabric3.transaction.jndi;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Property;

/**
 * Proxy to the JNDI-based transaction manager.
 * 
 * @version $Revision$ $Date$
 */
@EagerInit
public class JndiTransactionManagerProxy implements TransactionManager {
    
    // JNDI name of the transaction manager
    private String jndiName;
    
    // Provider URL
    private String providerUrl;
    
    // Initial context factory
    private String initialContextFactory;
    
    // Transaction manager
    private TransactionManager delegate;
    
    /**
     * JNDI name of the transaction manager.
     * 
     * @param jndiName JNDI name of the transaction manager.
     */
    @Property(required = true)
    public void setJndiName(String jndiName) {
        this.jndiName = jndiName;
    }
    
    /**
     * Provider URL for JNDI.
     * 
     * @param providerUrl Provider URL.
     */
    public void setProviderUrl(String providerUrl) {
        this.providerUrl = providerUrl;
    }
    
    /**
     * Initial context factory.
     * 
     * @param initialContextFactory Initial context factory.
     */
    public void setInitialConextFactory(String initialContextFactory) {
        this.initialContextFactory = initialContextFactory;
    }
    
    /**
     * Looks up the transaction manager.
     * @throws NamingException In case JNDI operation fails.
     */
    @Init
    public void init() throws NamingException {
        
        Hashtable<String, String> env = new Hashtable<String, String>();
        if(providerUrl != null) {
            env.put(Context.PROVIDER_URL, providerUrl);
        }
        if(initialContextFactory != null) {
            env.put(Context.INITIAL_CONTEXT_FACTORY, initialContextFactory);
        }
        
        Context context = null;
        
        try {
            context = new InitialContext(env);
            delegate = (TransactionManager) context.lookup(jndiName);
        } finally {
            if(context != null) {
                context.close();
            }
        }
        
    }

    /**
     * @see javax.transaction.TransactionManager#begin()
     */
    public void begin() throws NotSupportedException, SystemException {
        delegate.begin();
    }

    /**
     * @see javax.transaction.TransactionManager#commit()
     */
    public void commit() throws HeuristicMixedException, HeuristicRollbackException, IllegalStateException, RollbackException, SecurityException, SystemException {
        delegate.commit();
    }

    /**
     * @see javax.transaction.TransactionManager#getStatus()
     */
    public int getStatus() throws SystemException {
        return delegate.getStatus();
    }

    /**
     * @see javax.transaction.TransactionManager#getTransaction()
     */
    public Transaction getTransaction() throws SystemException {
        return delegate.getTransaction();
    }

    /**
     * @see javax.transaction.TransactionManager#resume(javax.transaction.Transaction)
     */
    public void resume(Transaction transaction) throws IllegalStateException, InvalidTransactionException, SystemException {
        delegate.resume(transaction);
    }

    /**
     * @see javax.transaction.TransactionManager#rollback()
     */
    public void rollback() throws IllegalStateException, SecurityException, SystemException {
        delegate.rollback();
    }

    /**
     * @see javax.transaction.TransactionManager#setRollbackOnly()
     */
    public void setRollbackOnly() throws IllegalStateException, SystemException {
        delegate.setRollbackOnly();
    }

    /**
     * @see javax.transaction.TransactionManager#setTransactionTimeout(int)
     */
    public void setTransactionTimeout(int timeout) throws SystemException {
        delegate.setTransactionTimeout(timeout);
    }

    /**
     * @see javax.transaction.TransactionManager#suspend()
     */
    public Transaction suspend() throws SystemException {
        return delegate.suspend();
    }

}
