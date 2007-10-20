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
package org.fabric3.messaging.jms;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.fabric3.spi.services.messaging.RequestListener;
import org.fabric3.spi.util.stax.StaxUtil;

/**
 * Fabric3 JMS message listsner.
 * 
 * @version $Revision$ $Date$
 */
public class Fabric3MessageListener implements MessageListener {

    // Discovery service
    private JmsMessagingService messagingService;
    
    // Runtime id
    private String runtimeId;
    
    /**
     * Initializes the discovery service.
     * @param messagingService messaging service.
     */
    public Fabric3MessageListener(JmsMessagingService messagingService, String runtimeId) {
        this.messagingService = messagingService;
        this.runtimeId = runtimeId;
    }
    
    /**
     * Message listener callback.
     */
    public void onMessage(Message message) {

        try {
            
            // TODO investigate why selectors are not working
            if(!runtimeId.equals(message.getStringProperty("runtimeId"))) {
                return;
            }
            
            final TextMessage textMessage = (TextMessage)message;
            final String text = textMessage.getText();
            
            final QName messageType = StaxUtil.getDocumentElementQName(text);
            System.err.println("Message received: " + messageType);
            
            RequestListener messageListener = messagingService.getRequestListener(messageType);
            if (messageListener != null) {
                XMLStreamReader requestReader = StaxUtil.createReader(text);
                messageListener.onRequest(requestReader);
            }

        } catch (JMSException ex) {
            throw new Fabric3JmsException(ex);
        } catch (XMLStreamException ex) {
            throw new Fabric3JmsException(ex);
        }

    }

}
