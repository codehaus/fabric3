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
import javax.transaction.Transaction;

import org.osoa.sca.Conversation;

/**
 * Responsible for returning an EntityManager with a persitence context tied to an execution context.
 *
 * @version $Revision$ $Date$
 */
public interface EntityManagerService {

    /**
     * Returns the EntityManager associated with the given transaction.
     *
     * @param unitName    the persistence unit name
     * @param proxy       the proxy requesting the EntityManager
     * @param transaction the transaction
     * @return the EntityManager
     * @throws EntityManagerCreationException if an error creating the EntityManager is encountered
     */
    EntityManager getEntityManager(String unitName, EntityManagerProxy proxy, Transaction transaction) throws EntityManagerCreationException;

    /**
     * Returns the EntityManager associated with the given conversation.
     *
     * @param unitName     the persistence unit name
     * @param proxy        the proxy requesting the EntityManager
     * @param conversation the conversation
     * @return the EntityManager
     * @throws EntityManagerCreationException if an error creating the EntityManager is encountered
     */
    EntityManager getEntityManager(String unitName, EntityManagerProxy proxy, Conversation conversation) throws EntityManagerCreationException;

}
