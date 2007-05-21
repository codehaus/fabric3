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

package org.fabric3.binding.jms.wire.helper;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;

/**
 * Helper class for JMS ops.
 */
public class JmsHelper {
    
    /**
     * Utility class constructor.
     */
    private JmsHelper() {
    }

    /**
     * Closes connections quietly.
     * @param connection Connection to be closed.
     */
    public static void closeQuietly(Connection connection) {                
        try {            
            if(connection != null) {
                connection.close();
            }            
        } catch(JMSException ignore) {
        }        
    }

    /**
     * Closes sessions quietly.
     * @param session Connection to be closed.
     */
    public static void closeQuietly(Session session) {                
        try {            
            if(session != null) {
                session.close();
            }            
        } catch(JMSException ignore) {
        }        
    }

    /**
     * Closes message producer quietly.
     * @param producer Message producer to be closed.
     */
    public static void closeQuietly(MessageProducer producer) {                
        try {            
            if(producer != null) {
                producer.close();
            }            
        } catch(JMSException ignore) {
        }        
    }

    /**
     * Closes message consumer quietly.
     * @param producer Message consumer to be closed.
     */
    public static void closeQuietly(MessageConsumer consumer) {                
        try {            
            if(consumer != null) {
                consumer.close();
            }            
        } catch(JMSException ignore) {
        }        
    }

}
