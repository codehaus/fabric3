/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.jpa.runtime;

import javax.persistence.EntityManager;
import javax.transaction.Transaction;

import org.fabric3.spi.component.F3Conversation;

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
    EntityManager getEntityManager(String unitName, EntityManagerProxy proxy, F3Conversation conversation) throws EntityManagerCreationException;

}
