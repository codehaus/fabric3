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
package org.fabric3.fabric.services.transaction;

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

import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

import org.fabric3.host.runtime.HostInfo;

/**
 * Transaction manager proxy. This class will proxy an injected transaction manager. If the delegate instance is not
 * injected, it will try to lookup the transaction manager from a JNDI namespace. By default the name used for lookup is
 * <code>javax/transaction/TransactionManager</code>. This class also propogates a uniform exception.
 *
 * @version $Revision$ $Date$
 */
public class TransactionManagerProxy implements TransactionManager {

    // JNDI name property
    private static final String TXM_JNDI_NAME = "fabric3.jta.txm.jndi.name";

    // JNDI name of the transaction manager
    private String jndiName;

    // Provider URL
    private String providerUrl;

    // Initial context factory
    private String initialContextFactory;

    // Host info
    private HostInfo hostInfo;

    // Transaction manager
    private TransactionManager delegate;

    /**
     * Inject host info.
     *
     * @param hostInfo Host info for the runtime environment.
     */
    @Reference(required = true)
    public void setHostInfo(HostInfo hostInfo) {
        this.hostInfo = hostInfo;
    }

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
     *
     * @throws NamingException In case JNDI operation fails.
     */
    @Init
    public void init() throws NamingException {

        Hashtable<String, String> env = new Hashtable<String, String>();
        if (providerUrl != null) {
            env.put(Context.PROVIDER_URL, providerUrl);
        }
        if (initialContextFactory != null) {
            env.put(Context.INITIAL_CONTEXT_FACTORY, initialContextFactory);
        }

        if (jndiName == null) {
            jndiName = hostInfo.getProperty(TXM_JNDI_NAME, "javax/transaction/TransactionManager");
        }
        Context context = null;

        try {
            context = new InitialContext(env);
            delegate = (TransactionManager) context.lookup(jndiName);
        } finally {
            if (context != null) {
                context.close();
            }
        }

    }

    public void begin() throws NotSupportedException, SystemException {
        delegate.begin();
    }

    public void commit()
            throws HeuristicMixedException, HeuristicRollbackException, RollbackException, SystemException {
        delegate.commit();
    }

    public int getStatus() throws SystemException {
        return delegate.getStatus();
    }

    public Transaction getTransaction() throws SystemException {
        return delegate.getTransaction();
    }

    public void resume(Transaction transaction) throws InvalidTransactionException, SystemException {
        delegate.resume(transaction);
    }

    public void rollback() throws SystemException {
        delegate.rollback();
    }

    public void setRollbackOnly() throws SystemException {
        delegate.setRollbackOnly();
    }

    public void setTransactionTimeout(int timeout) throws SystemException {
        delegate.setTransactionTimeout(timeout);
    }

    public Transaction suspend() throws SystemException {
        return delegate.suspend();
    }

}
