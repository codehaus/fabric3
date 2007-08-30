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
package org.fabric3.tx.proxy;

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

import org.fabric3.tx.TxException;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Property;

/**
 * Transaction manager proxy. This class will proxy an injected transaction manager. If the delegate 
 * instance is not injected, it will try to lookup the transaction manager from a JNDI namespace. By 
 * default the name used for lookup is <code>javax/transaction/TransactionManager</code>. This class 
 * also propogates a uniform exception.
 * 
 * @version $Revision$ $Date$
 */
@EagerInit
public class TransactionManagerProxy implements TransactionManager {
    
    // JNDI name of the transaction manager
    private String jndiName = "javax/transaction/TransactionManager";
    
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
    @Property
    public void setJndiName(String jndiName) {
        this.jndiName = jndiName;
    }
    
    /**
     * Provider URL for JNDI.
     * 
     * @param providerUrl Provider URL.
     */
    @Property
    public void setProviderUrl(String providerUrl) {
        this.providerUrl = providerUrl;
    }
    
    /**
     * Initial context factory.
     * 
     * @param initialContextFactory Initial context factory.
     */
    @Property
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
    public void begin() throws TxException {
        try {
            delegate.begin();
        } catch (NotSupportedException e) {
            throw new TxException(e);
        } catch (SystemException e) {
            throw new TxException(e);
        }
    }

    /**
     * @see javax.transaction.TransactionManager#commit()
     */
    public void commit() throws TxException {
        try {
            delegate.commit();
        } catch (HeuristicMixedException e) {
            throw new TxException(e);
        } catch (HeuristicRollbackException e) {
            throw new TxException(e);
        } catch (RollbackException e) {
            throw new TxException(e);
        } catch (SystemException e) {
            throw new TxException(e);
        }
    }

    /**
     * @see javax.transaction.TransactionManager#getStatus()
     */
    public int getStatus() throws TxException {
        try {
            return delegate.getStatus();
        } catch (SystemException e) {
            throw new TxException(e);
        }
    }

    /**
     * @see javax.transaction.TransactionManager#getTransaction()
     */
    public Transaction getTransaction() throws TxException {
        try {
            return delegate.getTransaction();
        } catch (SystemException e) {
            throw new TxException(e);
        }
    }

    /**
     * @see javax.transaction.TransactionManager#resume(javax.transaction.Transaction)
     */
    public void resume(Transaction transaction) throws TxException {
        try {
            delegate.resume(transaction);
        } catch (InvalidTransactionException e) {
            throw new TxException(e);
        } catch (SystemException e) {
            throw new TxException(e);
        }
    }

    /**
     * @see javax.transaction.TransactionManager#rollback()
     */
    public void rollback() throws TxException {
        try {
            delegate.rollback();
        } catch (SecurityException e) {
            throw new TxException(e);
        } catch (SystemException e) {
            throw new TxException(e);
        }
    }

    /**
     * @see javax.transaction.TransactionManager#setRollbackOnly()
     */
    public void setRollbackOnly() throws TxException {
        try {
            delegate.setRollbackOnly();
        } catch (SystemException e) {
            throw new TxException(e);
        }
    }

    /**
     * @see javax.transaction.TransactionManager#setTransactionTimeout(int)
     */
    public void setTransactionTimeout(int timeout) throws TxException {
        try {
            delegate.setTransactionTimeout(timeout);
        } catch (SystemException e) {
            throw new TxException(e);
        }
    }

    /**
     * @see javax.transaction.TransactionManager#suspend()
     */
    public Transaction suspend() throws TxException {
        try {
            return delegate.suspend();
        } catch (SystemException e) {
            throw new TxException(e);
        }
    }

}
