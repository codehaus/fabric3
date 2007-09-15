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

import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;

import org.fabric3.binding.jms.tx.TransactionHandler;

/**
 * @version $Revision$ $Date$
 */
public class ConsumerWorker implements Runnable {
    
    private final Session session;
    private final TransactionHandler transactionHandler;
    private final MessageConsumer consumer;
    private final MessageListener listener;
    private final long readTimeout;

    /**
     * @param session Session used to receive messages.
     * @param transactionHandler Transaction handler.
     * @param consumer Message consumer.
     * @param listener Delegate message listener.
     * @param readTimeout Read timeout.
     */
    public ConsumerWorker(Session session, 
                          TransactionHandler transactionHandler, 
                          MessageConsumer consumer, 
                          MessageListener listener,
                          long readTimeout) {
        this.session = session;
        this.transactionHandler = transactionHandler;
        this.consumer = consumer;
        this.listener = listener;
        this.readTimeout = readTimeout;
    }

    /**
     * @see java.lang.Runnable#run()
     */
    public void run() {
        try {
            transactionHandler.enlist(session);
            Message message = consumer.receive(readTimeout);
            if(message != null) {
                listener.onMessage(message);
            }
            transactionHandler.commit(session);
        } catch(Exception ex) {
            transactionHandler.rollback(session);
            // TODO use the monitor
            ex.printStackTrace();
        }
    }

}
