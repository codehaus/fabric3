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
 * --- Original Apache License ---
 *
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
package org.fabric3.binding.jms.runtime;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;

import org.fabric3.binding.jms.runtime.helper.JmsHelper;

/**
 * Factory for providing connections, sessions and destinations.
 */
public class JmsFactory {
    private static final int CACHE_CONNECTION = 1;
    private static final int CACHE_SESSION = 2;

    private final ConnectionFactory connectionFactory;

    /**
     * Cache level
     */
    private int cacheLevel = 1;

    /**
     * JMS connection shared by session
     */
    private Connection sharedConnection;
    /**
     * JMS Session shared by consumer
     */
    private Session sharedSession;
    /**
     * JMS destination
     */
    private final Destination destination;


    public JmsFactory(ConnectionFactory connectionFactory, Destination destination, int cacheLevel) {
        this.connectionFactory = connectionFactory;
        this.destination = destination;
        this.cacheLevel = cacheLevel;
    }

    public Connection getConnection() throws JMSException {
        if (sharedConnection == null) {
            sharedConnection = connectionFactory.createConnection();
        }
        //TODO check connection
        return sharedConnection;
    }

    public Destination getDestination() {
        return destination;
    }

    public Session getSession() throws JMSException {
        if (sharedSession == null) {
            sharedSession = createTransactedSession();
        }
        //TODO check session
        return sharedSession;
    }

    public Session createTransactedSession() throws JMSException {
        return getConnection().createSession(true, Session.SESSION_TRANSACTED);
    }

    /**
     * Recycles shared connections and sessions.
     */
    public void recycle() {
        if (cacheLevel < CACHE_CONNECTION) {
            if (sharedConnection != null) {
                JmsHelper.closeQuietly(sharedConnection);
                sharedConnection = null;
            }
        }
        if (cacheLevel < CACHE_SESSION) {
            if (sharedSession != null) {
                //already closed by connection.
                sharedSession = null;
            }
        }
    }

    /**
     * Closes shared resources.
     */
    public void close() {
        if (sharedConnection != null) {
            JmsHelper.closeQuietly(sharedConnection);
        }
    }

}