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

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import net.jxta.impl.protocol.ResolverResponse;
import net.jxta.protocol.ResolverQueryMsg;
import net.jxta.protocol.ResolverResponseMsg;
import net.jxta.resolver.QueryHandler;
import net.jxta.resolver.ResolverService;

import org.fabric3.messaging.jxta.JxtaMessagingService;
import org.fabric3.messaging.jxta.JxtaException;
import org.fabric3.spi.services.messaging.RequestListener;
import org.fabric3.spi.services.messaging.ResponseListener;
import org.fabric3.spi.util.stax.StaxUtil;

/**
 * Generic quety handler for Fabric3 PRP (Peer Resolver Protocol) messages. The
 * <code>processQuery</code> method is invoked on the receiver and the <code>
 * processResponse</code> is invoked on the sender when the receiver responds.
 * @version $Revision$ $Date$
 *
 */
public class Fabric3QueryHandler implements QueryHandler {

    /** Resolver service for sending responses. */
    private final ResolverService resolverService;

    /** Discovery service. */
    private final JxtaMessagingService discoveryService;

    /**
     * Initializes the JXTA resolver service and Fabric3 discovery service.
     *
     * @param resolverService Resolver service.
     * @param discoveryService Fabric3 discovery service.
     */
    public Fabric3QueryHandler(final ResolverService resolverService, final JxtaMessagingService discoveryService) {
        this.resolverService = resolverService;
        this.discoveryService = discoveryService;
    }

    /**
     * Processes a query message.
     */
    public int processQuery(ResolverQueryMsg queryMessage) {

        try {

            final String message = queryMessage.getQuery();
            final int queryId = queryMessage.getQueryId();
            final String source = queryMessage.getSrc();
            final String handler = queryMessage.getHandlerName();

            final QName messageType = StaxUtil.getDocumentElementQName(message);
            RequestListener messageListener = discoveryService.getRequestListener(messageType);
            if(messageListener != null) {

                XMLStreamReader requestReader = StaxUtil.createReader(message);
                XMLStreamReader responseReader = messageListener.onRequest(requestReader);
                String response = StaxUtil.serialize(responseReader);

                ResolverResponse responseMessage = new ResolverResponse();
                responseMessage.setResponse(response);
                responseMessage.setHandlerName(handler);
                responseMessage.setQueryId(queryId);

                resolverService.sendResponse(source, responseMessage);

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

        try {

            final String message = responseMessage.getResponse();
            final int queryId = responseMessage.getQueryId();

            final QName messageType = StaxUtil.getDocumentElementQName(message);
            ResponseListener messageListener = discoveryService.getResponseListener(messageType);
            if(messageListener != null) {
                XMLStreamReader responseReader = StaxUtil.createReader(message);
                messageListener.onResponse(responseReader, queryId);
            }

        } catch(XMLStreamException ex) {
            throw new JxtaException(ex);
        }

    }

}
