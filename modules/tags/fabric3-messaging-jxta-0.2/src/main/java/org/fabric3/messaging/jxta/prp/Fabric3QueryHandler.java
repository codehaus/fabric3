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
package org.fabric3.messaging.jxta.prp;

import java.io.Reader;
import java.io.StringReader;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import net.jxta.protocol.ResolverQueryMsg;
import net.jxta.protocol.ResolverResponseMsg;
import net.jxta.resolver.QueryHandler;
import net.jxta.resolver.ResolverService;

import org.fabric3.messaging.jxta.JxtaException;
import org.fabric3.messaging.jxta.JxtaMessagingService;
import org.fabric3.spi.services.messaging.RequestListener;

/**
 * Generic quety handler for Fabric3 PRP (Peer Resolver Protocol) messages. The
 * <code>processQuery</code> method is invoked on the receiver and the <code>
 * processResponse</code> is invoked on the sender when the receiver responds.
 * @version $Revision$ $Date$
 *
 */
public class Fabric3QueryHandler implements QueryHandler {

    /** Discovery service. */
    private final JxtaMessagingService messagingService;

    private final XMLInputFactory xmlFactory;

    /**
     * Initializes the JXTA resolver service and Fabric3 discovery service.
     *
     * @param messagingService Fabric3 messaging service.
     */
    public Fabric3QueryHandler(final JxtaMessagingService messagingService) {
        this.messagingService = messagingService;
        xmlFactory = XMLInputFactory.newInstance("javax.xml.stream.XMLInputFactory", getClass().getClassLoader());
    }

    /**
     * Processes a query message.
     */
    public int processQuery(ResolverQueryMsg queryMessage) {

        try {

            final String message = queryMessage.getQuery();
            Reader reader = new StringReader(message);
            XMLStreamReader xmlReader = xmlFactory.createXMLStreamReader(reader);
            //noinspection StatementWithEmptyBody
            while (xmlReader.next() != XMLStreamConstants.START_ELEMENT);

            final QName messageType = xmlReader.getName();
            RequestListener messageListener = messagingService.getRequestListener(messageType);
            if(messageListener != null) {
                messageListener.onRequest(xmlReader);

            }
            return ResolverService.OK;

        } catch(XMLStreamException ex) {
            throw new JxtaException(ex);
        }

    }

    /**
     * Processes a response message.
     */
    public void processResponse(ResolverResponseMsg responseMessage) {
    }

}
