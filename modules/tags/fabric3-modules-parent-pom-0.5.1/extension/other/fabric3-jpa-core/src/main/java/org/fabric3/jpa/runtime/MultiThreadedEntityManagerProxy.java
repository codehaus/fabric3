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

import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.Query;
import javax.persistence.EntityTransaction;
import javax.transaction.TransactionManager;
import javax.transaction.Transaction;
import javax.transaction.SystemException;

import org.osoa.sca.Conversation;
import org.osoa.sca.ServiceRuntimeException;

import org.fabric3.pojo.PojoWorkContextTunnel;
import org.fabric3.spi.invocation.WorkContext;

/**
 * An EntityManager proxy that delegates to a backing instance. This proxy is injected on composite-scoped components where more than one thread may
 * be accessing the proxy at a time.
 * <p/>
 * If the persistence context is transaction-scoped (as defined by JPA), the proxy will attempt to retrieve the EntityManager instance associated with
 * the current transaction context from the EntityManagerService. If the persistence context is extended (as defined by JPA), the proxy will attempt
 * to retrieve the EntityManager instance associated with the current conversation.
 *
 * @version $Revision$ $Date$
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
            WorkContext context = PojoWorkContextTunnel.getThreadWorkContext();
            Conversation conversation = context.peekCallFrame().getConversation();
            if (conversation == null) {
                throw new IllegalStateException("No conversational context associated with the current thread");
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
                return emService.getEntityManager(unitName, this, trx);
            } catch (SystemException e) {
                throw new ServiceRuntimeException(e);
            } catch (EntityManagerCreationException e) {
                throw new ServiceRuntimeException(e);
            }
        }
    }

}