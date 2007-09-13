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

import java.util.List;
import java.util.Stack;

import javax.jms.JMSException;
import javax.jms.ServerSession;
import javax.jms.ServerSessionPool;
import javax.jms.Session;

import org.fabric3.binding.jms.tx.TransactionHandler;

/**
 * Server session pool used by the standalone JMS server.
 * 
 * @version $Revision$ $Date$
 */
public class StandaloneServerSessionPool implements ServerSessionPool {
    
    // Available server sessions
    private Stack<ServerSession> serverSessions = new Stack<ServerSession>();
    
    /**
     * Initializes the server sessions.
     * @param serverSessions Server sessions.
     */
    public StandaloneServerSessionPool(List<Session> sessions, TransactionHandler transactionHandler) {
        for(Session session : sessions) {
            ServerSession serverSession = new StandaloneServerSession(session, this, transactionHandler);
            this.serverSessions.push(serverSession);
        }
    }
    
    /**
     * Closes the underlying sessions.
     */
    public void stop() throws JMSException {
        ServerSession serverSession = null;
        while((serverSession = getServerSession()) != null) {
            serverSession.getSession().close();
        }
    }

    /**
     * @see javax.jms.ServerSessionPool#getServerSession()
     */
    public ServerSession getServerSession() throws JMSException {
        
        synchronized (serverSessions) {
            
            while(serverSessions.isEmpty()) {
                try {
                    serverSessions.wait();
                } catch (InterruptedException e) {
                    throw new JMSException("Unable to get a server session");
                }
            }
            return serverSessions.pop();
            
        }
        
    }
    
    /**
     * Returns the session to the pool.
     * @param serverSession Server session to be returned.
     */
    protected void returnSession(ServerSession serverSession) {
        
        synchronized (serverSessions) {
            serverSessions.push(serverSession);
            serverSessions.notify();
        }
        
    }

}
