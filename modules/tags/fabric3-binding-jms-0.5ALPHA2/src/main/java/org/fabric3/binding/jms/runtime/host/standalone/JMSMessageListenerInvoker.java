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
package org.fabric3.binding.jms.runtime.host.standalone;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ServerSessionPool;
import javax.jms.Session;

import org.fabric3.binding.jms.common.Fabric3JmsException;
import org.fabric3.binding.jms.common.TransactionType;
import org.fabric3.binding.jms.runtime.JMSObjectFactory;
import org.fabric3.binding.jms.runtime.ResponseMessageListener;
import org.fabric3.binding.jms.runtime.tx.TransactionHandler;
import org.fabric3.spi.services.work.WorkScheduler;
/**
 *
 * A container class used to support MessageListener with ServerSessionPool.
 *
 */
public class JMSMessageListenerInvoker implements MessageListener {
    /** Request JMS object factory*/
    private JMSObjectFactory requestJMSObjectFactory = null;
    /** Response JMS object factory*/
    private JMSObjectFactory responseJMSObjectFactory;
    /** ResponseMessageListenerImpl invoked by this invoker */
    private ResponseMessageListener messageListener = null;
    /** Transaction Type */
    private TransactionType transactionType;
    /** Transaction Handler*/
    private TransactionHandler transactionHandler;
    /** WorkScheduler passed to serverSessionPool */
    private WorkScheduler workScheduler;

    public JMSMessageListenerInvoker(JMSObjectFactory requestJMSObjectFactory,
            JMSObjectFactory responseJMSObjectFactory,
            ResponseMessageListener messageListener,
            TransactionType transactionType,
            TransactionHandler transactionHandler, WorkScheduler workScheduler) {
        this.requestJMSObjectFactory = requestJMSObjectFactory;
        this.responseJMSObjectFactory = responseJMSObjectFactory;
        this.messageListener = messageListener;
        this.transactionType = transactionType;
        this.transactionHandler = transactionHandler;
        this.workScheduler = workScheduler;
    }

    public void start(int receiverCount) {
        ServerSessionPool serverSessionPool = createServerSessionPool(receiverCount);
        try {
            Connection connection = requestJMSObjectFactory.getConnection();
            connection.createConnectionConsumer(requestJMSObjectFactory
                    .getDestination(), null, serverSessionPool, 1);
            connection.start();
        } catch (JMSException e) {
            throw new Fabric3JmsException("Error when register Listener",e);

        }
    }

    private StandaloneServerSessionPool createServerSessionPool(int receiverCount) {
        return new StandaloneServerSessionPool(requestJMSObjectFactory,
                transactionHandler, this, transactionType,workScheduler,receiverCount);
    }

    public void stop() {
        requestJMSObjectFactory.close();
        responseJMSObjectFactory.close();
    }

    public void onMessage(Message message) {
        try {
            Session responseSession = responseJMSObjectFactory.createSession();
            if (transactionType == TransactionType.GLOBAL) {
                transactionHandler.enlist(responseSession);
            }
            Destination responseDestination = responseJMSObjectFactory
                    .getDestination();
            messageListener.onMessage(message, responseSession,
                    responseDestination);
            if (transactionType == TransactionType.GLOBAL) {
                transactionHandler.commit();
            }else if(transactionType == TransactionType.LOCAL){
                responseSession.commit();
            }
            responseJMSObjectFactory.recycle();
        } catch (JMSException e) {
            throw new Fabric3JmsException("Error when invoking Listener",e);
        } catch (RuntimeException e) {
            try{
                if (transactionType == TransactionType.GLOBAL) {
                    transactionHandler.rollback();
                }
            }catch(Exception ne){
                //ignore
                ne.printStackTrace();
            }
            e.printStackTrace();
            throw e;
        }
    }


}
