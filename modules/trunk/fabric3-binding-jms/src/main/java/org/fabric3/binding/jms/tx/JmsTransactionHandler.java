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
package org.fabric3.binding.jms.tx;

import java.util.HashSet;
import java.util.Set;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Session;

import org.fabric3.binding.jms.helper.JmsHelper;

/**
 * @version $Revision$ $Date$
 */
public class JmsTransactionHandler implements TransactionHandler {
    
    private ThreadLocal<Set<Session>> sessions = new ThreadLocal<Set<Session>>() {
        protected synchronized Set<Session> initialValue() {
            return new HashSet<Session>();
        }
    };

    /**
     * @see org.fabric3.binding.jms.tx.TransactionHandler#begin(javax.jms.Session)
     */
    public void enlist(Session session) throws JmsTxException {
        sessions.get().add(session);
    }

    /**
     * @see org.fabric3.binding.jms.tx.TransactionHandler#commit(javax.jms.Session)
     */
    public void commit() throws JmsTxException {
        try {
            for(Session session : sessions.get()) {
                session.commit();
                JmsHelper.closeQuietly(session);
            }
        } catch (JMSException e) {
            throw new JmsTxException(e);
        } finally {
            sessions.remove();
        }
    }

    /**
     * @see org.fabric3.binding.jms.tx.TransactionHandler#rollback(javax.jms.Session)
     */
    public void rollback() throws JmsTxException {
        try {
            for(Session session : sessions.get()) {
                session.rollback();
                JmsHelper.closeQuietly(session);
            }
        } catch (JMSException e) {
            throw new JmsTxException(e);
        } finally {
            sessions.remove();
        }
    }

    /**
     * @see org.fabric3.binding.jms.tx.TransactionHandler#createSession(javax.jms.Connection)
     */
    public Session createSession(Connection con) throws JmsTxException {
        
        try {   
            return con.createSession(true, Session.SESSION_TRANSACTED);
        } catch(JMSException e) {
            throw new JmsTxException(e);
        }
        
    }

}
