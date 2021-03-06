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
package org.fabric3.binding.jms.runtime.lookup.destination;

import java.util.Hashtable;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.QueueConnection;
import javax.jms.Session;
import javax.jms.TopicConnection;

import org.fabric3.binding.jms.common.DestinationDefinition;
import org.fabric3.binding.jms.runtime.helper.JmsHelper;
import org.fabric3.binding.jms.runtime.lookup.JmsLookupException;

/**
 * Implementation that attempts to resolve the destination in JNDI.
 */
public class AlwaysDestinationStrategy implements DestinationStrategy {

    public Destination getDestination(DestinationDefinition definition, ConnectionFactory cf, Hashtable<String, String> env)
            throws JmsLookupException {
        Connection connection = null;
        try {
            String name = definition.getName();
            connection = cf.createConnection();

            switch (definition.getDestinationType()) {
            case queue:
                QueueConnection qc = (QueueConnection) connection;
                return qc.createQueueSession(false, Session.AUTO_ACKNOWLEDGE).createQueue(name);
            case topic:
                TopicConnection tc = (TopicConnection) connection;
                return tc.createTopicSession(false, Session.AUTO_ACKNOWLEDGE).createTopic(name);
            default:
                throw new IllegalArgumentException("Unknown destination type");
            }
        } catch (JMSException ex) {
            throw new JmsLookupException("Unable to create destination", ex);
        } finally {
            JmsHelper.closeQuietly(connection);
        }
    }
}
