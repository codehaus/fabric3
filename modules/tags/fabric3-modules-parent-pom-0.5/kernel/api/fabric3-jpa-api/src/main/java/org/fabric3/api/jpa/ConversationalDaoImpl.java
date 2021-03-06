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
package org.fabric3.api.jpa;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.Query;

import org.osoa.sca.annotations.Scope;

/**
 *
 * @version $Revision$ $Date$
 */
@Scope("CONVERSATION")
public class ConversationalDaoImpl<ENTITY, KEY> implements ConversationalDao<ENTITY, KEY> {
    
    protected EntityManager entityManager;

    public void close() {
        // No op
    }

    public ENTITY findById(Class<ENTITY> entityClass, KEY key) {
        return entityManager.find(entityClass, key);
    }

    public ENTITY merge(ENTITY entity) {
        return entityManager.merge(entity);
    }

    public void persist(ENTITY entity) {
        entityManager.persist(entity);
    }

    public void refresh(ENTITY entity) {
        entityManager.refresh(entity);
    }

    public void remove(ENTITY entity) {
        entityManager.remove(entity);
    }

    @SuppressWarnings("unchecked")
    public List<ENTITY> findByNamedQuery(String namedQuery, Class<ENTITY> entityClass, Object... args) {
        
        Query query = entityManager.createNamedQuery(namedQuery);
        int index = 0;
        for (Object arg : args) {
            query.setParameter(++index, arg);
        }
        return (List<ENTITY>) query.getResultList();
    }

    public void flush() {
        entityManager.flush();
    }

    public void lock(ENTITY entity, LockModeType lockModeType) {
        entityManager.lock(entity, lockModeType);
    }

}
