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
package org.fabric3.jpa.runtime;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.Transaction;
import javax.transaction.Synchronization;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

import org.osoa.sca.Conversation;
import org.osoa.sca.annotations.Reference;

import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.component.ConversationExpirationCallback;

/**
 * Implementation that manages a cache of EntityManagers.
 *
 * @version $Revision$ $Date$
 */
public class EntityManagerServiceImpl implements EntityManagerService {
    public static final Object JOINED = new Object();
    private Map<Object, EntityManager> cache = new ConcurrentHashMap<Object, EntityManager>();
    // tracks which transactions have joined entity managers
    private Map<Transaction, Object> joinedTransaction = new ConcurrentHashMap<Transaction, Object>();
    private EmfCache emfCache;
    private TransactionManager tm;
    private ScopeContainer<Conversation> scopeContainer;

    public EntityManagerServiceImpl(@Reference EmfCache emfCache,
                                    @Reference TransactionManager tm,
                                    @Reference ScopeContainer<Conversation> scopeContainer) {
        this.emfCache = emfCache;
        this.tm = tm;
        this.scopeContainer = scopeContainer;
    }

    public EntityManager getEntityManager(String unitName, EntityManagerProxy proxy, Transaction transaction) throws EntityManagerCreationException {
        EntityManager em = cache.get(transaction);
        if (em == null) {
            EntityManagerFactory emf = emfCache.getEmf(unitName);
            if (emf == null) {
                throw new EntityManagerCreationException("No EntityManagerFactory found for persistence unit: " + unitName);
            }
            em = emf.createEntityManager();
            // don't synchronize on the transaction since it can assume to be bound to a thread at this point
            registerSync(proxy, transaction, false);
            cache.put(transaction, em);
        }
        return em;
    }

    public EntityManager getEntityManager(String unitName, EntityManagerProxy proxy, Conversation conversation)
            throws EntityManagerCreationException {
        EntityManager em = cache.get(conversation);
        if (em == null) {
            try {
                EntityManagerFactory emf = emfCache.getEmf(unitName);
                if (emf == null) {
                    throw new EntityManagerCreationException("No EntityManagerFactory found for persistence unit: " + unitName);
                }
                // don't synchronize on the transaction since it can assume to be bound to a thread at this point
                em = emf.createEntityManager();
                Transaction transaction = tm.getTransaction();
                boolean mustJoin = !joinedTransaction.containsKey(transaction);
                scopeContainer.registerCallback(conversation, new JPACallback(proxy, transaction, mustJoin));
                // A transaction synchronization needs to be registered so that the proxy can clear the EM after the transaction commits.
                // This is necessary so joinsTransaction can be called
                registerSync(proxy, transaction, mustJoin);
                if (mustJoin) {
                    // join the current transaction. This only needs to be done for extended persistence conttexts
                    em.joinTransaction();
                    joinedTransaction.put(transaction, JOINED);
                }
                cache.put(conversation, em);
            } catch (SystemException e) {
                throw new EntityManagerCreationException(e);
            }
        }
        return em;
    }

    private void registerSync(EntityManagerProxy proxy, Transaction transaction, boolean joined) throws EntityManagerCreationException {
        try {
            TrxSync sync = new TrxSync(proxy, transaction, joined);
            transaction.registerSynchronization(sync);
        } catch (RollbackException e) {
            throw new EntityManagerCreationException(e);
        } catch (SystemException e) {
            throw new EntityManagerCreationException(e);
        }
    }

    /**
     * Callback used with a transaction-scoped EntityManager to remove it from the cache and close it.
     */
    private class TrxSync implements Synchronization {
        private Transaction transaction;
        private EntityManagerProxy proxy;
        private boolean joined;

        private TrxSync(EntityManagerProxy proxy, Transaction transaction, boolean joined) {
            this.transaction = transaction;
            this.proxy = proxy;
            this.joined = joined;
        }

        public void beforeCompletion() {

        }

        public void afterCompletion(int status) {
            cache.remove(transaction);
            if (joined) {
                joinedTransaction.remove(transaction);
            }
            proxy.clearEntityManager();
            // TODO check that the JPA provider closes the EntityManager instance, since it is not closed here
        }
    }

    /**
     * Callback used with an extended persistence context EntityManager to remove it from the cache and close it.
     */
    private class JPACallback implements ConversationExpirationCallback {
        private EntityManagerProxy proxy;
        private Transaction transaction;
        private boolean joined;

        public JPACallback(EntityManagerProxy proxy, Transaction transaction, boolean joined) {
            this.proxy = proxy;
            this.transaction = transaction;
            this.joined = joined;
        }

        public void expire(Conversation conversation) {
            EntityManager em = cache.remove(conversation);
            if (joined) {
                joinedTransaction.remove(transaction);
            }
            assert em != null;
            proxy.clearEntityManager();
            em.close();
        }
    }
}
