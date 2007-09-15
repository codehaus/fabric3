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
package org.fabric3.binding.jms.host.standalone;

import javax.jms.JMSException;
import javax.jms.ServerSession;
import javax.jms.Session;

import org.fabric3.binding.jms.tx.TransactionHandler;

/**
 * Server session used in standalone JMS host.
 * 
 * @version $Revision$ $Date$
 */
public class StandaloneServerSession implements ServerSession {
    
    private StandaloneServerSessionPool serverSessionPool;
    private Session session;
    private TransactionHandler transactionHandler;
    
    /**
     * Initializes the server session.
     * 
     * @param session Underlying JMS session.
     * @param serverSessionPool Server session pool.
     * @param transactionHandler Transaction handler (XA or Local)
     */
    public StandaloneServerSession(Session session, StandaloneServerSessionPool serverSessionPool, TransactionHandler transactionHandler) {
        this.session = session;
        this.serverSessionPool = serverSessionPool;
        this.transactionHandler = transactionHandler;
    }

    /**
     * @see javax.jms.ServerSession#getSession()
     */
    public Session getSession() throws JMSException {
        return session;
    }

    /**
     * @see javax.jms.ServerSession#start()
     */
    public void start() throws JMSException {
        
        try {
            transactionHandler.enlist(session);
            session.run();
            transactionHandler.commit();
        } catch(RuntimeException ex) {
            transactionHandler.rollback();
            throw ex;
        } finally {
            serverSessionPool.returnSession(this);
        }
        
    }

}
