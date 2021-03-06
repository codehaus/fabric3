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
package org.fabric3.jpa.runtime;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.Query;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.oasisopen.sca.ServiceRuntimeException;

import org.fabric3.spi.invocation.F3Conversation;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.invocation.WorkContextTunnel;

/**
 * An EntityManager proxy that delegates to a backing instance. This proxy is injected on composite-scoped components where more than one thread may
 * be accessing the proxy at a time.
 * <p/>
 * If the persistence context is transaction-scoped (as defined by JPA), the proxy will attempt to retrieve the EntityManager instance associated with
 * the current transaction context from the EntityManagerService. If the persistence context is extended (as defined by JPA), the proxy will attempt
 * to retrieve the EntityManager instance associated with the current conversation.
 *
 * @version $Rev$ $Date$
 */
public class MultiThreadedEntityManagerProxy implements EntityManagerProxy {
    private String unitName;
    private boolean extended;
    private EntityManagerService emService;
    private TransactionManager tm;

    public MultiThreadedEntityManagerProxy(String unitName, boolean extended, EntityManagerService emService, TransactionManager tm) {
        this.unitName = unitName;
        this.extended = extended;
        this.emService = emService;
        this.tm = tm;
    }

    public void persist(Object entity) {
        getEntityManager().persist(entity);
    }

    public <T> T merge(T entity) {
        return getEntityManager().merge(entity);
    }

    public void remove(Object entity) {
        getEntityManager().remove(entity);
    }

    public <T> T find(Class<T> entityClass, Object primaryKey) {
        return getEntityManager().find(entityClass, primaryKey);
    }

    public <T> T getReference(Class<T> entityClass, Object primaryKey) {
        return getEntityManager().getReference(entityClass, primaryKey);
    }

    public void flush() {
        getEntityManager().flush();
    }

    public void setFlushMode(FlushModeType flushMode) {
        getEntityManager().setFlushMode(flushMode);
    }

    public FlushModeType getFlushMode() {
        return getEntityManager().getFlushMode();
    }

    public void lock(Object entity, LockModeType lockMode) {
        getEntityManager().lock(entity, lockMode);
    }

    public void refresh(Object entity) {
        getEntityManager().remove(entity);
    }

    public void clear() {
        getEntityManager().clear();
    }

    public boolean contains(Object entity) {
        return getEntityManager().contains(entity);
    }

    public Query createQuery(String qlString) {
        return getEntityManager().createQuery(qlString);
    }

    public Query createNamedQuery(String name) {
        return getEntityManager().createNamedQuery(name);
    }

    public Query createNativeQuery(String sqlString) {
        return getEntityManager().createNativeQuery(sqlString);
    }

    public Query createNativeQuery(String sqlString, Class resultClass) {
        return getEntityManager().createNativeQuery(sqlString, resultClass);
    }

    public Query createNativeQuery(String sqlString, String resultSetMapping) {
        return getEntityManager().createNativeQuery(sqlString, resultSetMapping);
    }

    public void joinTransaction() {
        getEntityManager().joinTransaction();
    }

    public Object getDelegate() {
        return getEntityManager().getDelegate();
    }

    public void close() {
        getEntityManager().close();
    }

    public boolean isOpen() {
        return getEntityManager().isOpen();
    }

    public EntityTransaction getTransaction() {
        return getEntityManager().getTransaction();
    }

    public void clearEntityManager() {

    }

    /**
     * Returns the delegated EntityManager. If the persistence context is transaction-scoped, the EntityManager associated with the current
     * transaction will be used. Otherwise, if the persistence context is extended, the EntityManager associated with the current conversation will be
     * used.
     *
     * @return the EntityManager
     */
    private EntityManager getEntityManager() {
        if (extended) {
            // an extended persistence context, associate it with the current conversation
            WorkContext context = WorkContextTunnel.getThreadWorkContext();
            F3Conversation conversation = context.peekCallFrame().getConversation();
            if (conversation == null) {
                throw new IllegalStateException("No conversational context associated with the current component");
            }
            try {
                return emService.getEntityManager(unitName, this, conversation);
            } catch (EntityManagerCreationException e) {
                throw new ServiceRuntimeException(e);
            }
        } else {
            // a transaction-scoped persitence context
            try {
                Transaction trx = tm.getTransaction();
                if (trx == null) {
                    throw new IllegalStateException("A transaction is not active - ensure the component is executing in a managed transaction");
                }
                return emService.getEntityManager(unitName, this, trx);
            } catch (SystemException e) {
                throw new ServiceRuntimeException(e);
            } catch (EntityManagerCreationException e) {
                throw new ServiceRuntimeException(e);
            }
        }
    }

}