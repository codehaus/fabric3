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
package org.fabric3.jpa.runtime;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.RollbackException;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.osoa.sca.Conversation;
import org.osoa.sca.annotations.Reference;

import org.fabric3.model.type.component.Scope;
import org.fabric3.spi.component.ConversationExpirationCallback;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.component.ScopeRegistry;

/**
 * Implementation that manages a cache of EntityManagers.
 *
 * @version $Revision$ $Date$
 */
public class EntityManagerServiceImpl implements EntityManagerService {
    public static final Object JOINED = new Object();
    // a chache of entity managers double keyed by scope (transaction or conversation) and persistence unit name
    private Map<Object, Map<String, EntityManager>> cache = new ConcurrentHashMap<Object, Map<String, EntityManager>>();
    // tracks which entity managers have joined transactions
    private Map<Transaction, Object> joinedTransaction = new ConcurrentHashMap<Transaction, Object>();
    private EmfCache emfCache;
    private TransactionManager tm;
    private ScopeContainer<Conversation> scopeContainer;

    public EntityManagerServiceImpl(@Reference EmfCache emfCache, @Reference TransactionManager tm, @Reference ScopeRegistry registry) {
        this.emfCache = emfCache;
        this.tm = tm;
        this.scopeContainer = registry.getScopeContainer(Scope.CONVERSATION);
    }

    public EntityManager getEntityManager(String unitName, EntityManagerProxy proxy, Transaction transaction) throws EntityManagerCreationException {
        // Note this method is threadsafe as a Transaction is only visible to a single thread at time.
        EntityManager em = null;
        Map<String, EntityManager> map = cache.get(transaction);
        if (map != null) {
            em = map.get(unitName);
        }

        if (em == null) {
            // no entity manager for the persistence unit associated with the transaction
            EntityManagerFactory emf = emfCache.getEmf(unitName);
            if (emf == null) {
                throw new EntityManagerCreationException("No EntityManagerFactory found for persistence unit: " + unitName);
            }
            em = emf.createEntityManager();
            // don't synchronize on the transaction since it can assume to be bound to a thread at this point
            registerTransactionScopedSync(proxy, unitName, transaction);
            if (map == null) {
                map = new ConcurrentHashMap<String, EntityManager>();
                cache.put(transaction, map);
            }
            map.put(unitName, em);
        }
        return em;
    }

    public EntityManager getEntityManager(String unitName, EntityManagerProxy proxy, Conversation conversation)
            throws EntityManagerCreationException {
        // synchronize on the conversation since multiple request threads may be active
        synchronized (conversation) {
            EntityManager em = null;
            Map<String, EntityManager> map = cache.get(conversation);
            if (map != null) {
                em = map.get(unitName);
            }

            if (em == null) {
                // no entity manager for the persistence unit associated with the conversation
                try {
                    EntityManagerFactory emf = emfCache.getEmf(unitName);
                    if (emf == null) {
                        throw new EntityManagerCreationException("No EntityManagerFactory found for persistence unit: " + unitName);
                    }
                    // don't synchronize on the transaction since it can assume to be bound to a thread at this point
                    em = emf.createEntityManager();
                    Transaction transaction = tm.getTransaction();
                    boolean mustJoin = !joinedTransaction.containsKey(transaction);
                    scopeContainer.registerCallback(conversation, new JPACallback(proxy, unitName, transaction, mustJoin));
                    // A transaction synchronization needs to be registered so that the proxy can clear the EM after the transaction commits.
                    // This is necessary so joinsTransaction is called for subsequent transactions
                    registerConversationScopedSync(proxy, transaction, mustJoin);
                    if (mustJoin) {
                        // join the current transaction. This only needs to be done for extended persistence conttexts
                        em.joinTransaction();
                        joinedTransaction.put(transaction, JOINED);
                    }
                    if (map == null) {
                        map = new ConcurrentHashMap<String, EntityManager>();
                        cache.put(conversation, map);
                    }
                    map.put(unitName, em);
                } catch (SystemException e) {
                    throw new EntityManagerCreationException(e);
                }
            }
            return em;
        }
    }

    private void registerTransactionScopedSync(EntityManagerProxy proxy, String unitName, Transaction transaction)
            throws EntityManagerCreationException {
        try {
            TransactionScopedSync sync = new TransactionScopedSync(proxy, unitName, transaction);
            transaction.registerSynchronization(sync);
        } catch (RollbackException e) {
            throw new EntityManagerCreationException(e);
        } catch (SystemException e) {
            throw new EntityManagerCreationException(e);
        }
    }

    private void registerConversationScopedSync(EntityManagerProxy proxy, Transaction transaction, boolean joined)
            throws EntityManagerCreationException {
        try {
            ConversationScopedSync sync = new ConversationScopedSync(proxy, transaction, joined);
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
    private class TransactionScopedSync implements Synchronization {
        private String unitName;
        private Transaction transaction;
        private EntityManagerProxy proxy;

        private TransactionScopedSync(EntityManagerProxy proxy, String unitName, Transaction transaction) {
            this.unitName = unitName;
            this.transaction = transaction;
            this.proxy = proxy;
        }

        public void beforeCompletion() {

        }

        public void afterCompletion(int status) {
            proxy.clearEntityManager();
            Map<String, EntityManager> map = cache.get(transaction);
            assert map != null;
            map.remove(unitName);
            // TODO check that the JPA provider closes the EntityManager instance, since it is not closed here
            if (map.isEmpty()) {
                cache.remove(transaction);
            }
        }
    }

    /**
     * Callback used with a conversation-scoped EntityManager to clear out EM proxies when a transaction completes (necessary for join transaction).
     */
    private class ConversationScopedSync implements Synchronization {
        private Transaction transaction;
        private EntityManagerProxy proxy;
        private boolean joined;

        private ConversationScopedSync(EntityManagerProxy proxy, Transaction transaction, boolean joined) {
            this.transaction = transaction;
            this.proxy = proxy;
            this.joined = joined;
        }

        public void beforeCompletion() {

        }

        public void afterCompletion(int status) {
            if (joined) {
                joinedTransaction.remove(transaction);
            }
            proxy.clearEntityManager();
            // note the EM cache is not cleared here as it is done when the JPACallback is invoked at conversation end
        }
    }


    /**
     * Callback used with an extended persistence context EntityManager to remove it from the cache and close it.
     */
    private class JPACallback implements ConversationExpirationCallback {
        private EntityManagerProxy proxy;
        private String unitName;
        private Transaction transaction;
        private boolean joined;

        public JPACallback(EntityManagerProxy proxy, String unitName, Transaction transaction, boolean joined) {
            this.proxy = proxy;
            this.unitName = unitName;
            this.transaction = transaction;
            this.joined = joined;
        }

        public void expire(Conversation conversation) {
            synchronized (conversation) {
                if (joined) {
                    joinedTransaction.remove(transaction);
                }
                proxy.clearEntityManager();
                Map<String, EntityManager> map = cache.get(conversation);
                assert map != null;
                EntityManager em = map.remove(unitName);
                assert em != null;
                em.close();
                if (map.isEmpty()) {
                    cache.remove(conversation);
                }
            }
        }
    }
}
