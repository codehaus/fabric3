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
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import net.jxta.protocol.ResolverQueryMsg;
import net.jxta.protocol.ResolverResponseMsg;
import net.jxta.resolver.QueryHandler;
import net.jxta.resolver.ResolverService;

import org.fabric3.messaging.jxta.JxtaException;
import org.fabric3.messaging.jxta.MessagingMonitor;
import org.fabric3.spi.services.messaging.MessagingEventService;

/**
 * Generic quety handler for Fabric3 PRP (Peer Resolver Protocol) messages. The <code>processQuery</code> method is invoked on the receiver and the
 * <code> processResponse</code> is invoked on the sender when the receiver responds.
 *
 * @version $Revision$ $Date$
 */
public class Fabric3QueryHandler implements QueryHandler {

    /**
     * Discovery service.
     */
    private final MessagingEventService eventService;
    private MessagingMonitor monitor;

    private final XMLInputFactory xmlFactory;

    /**
     * Initializes the JXTA resolver service and Fabric3 discovery service.
     *
     * @param eventService messaging event service.
     * @param monitor      the monitor
     */
    public Fabric3QueryHandler(MessagingEventService eventService, MessagingMonitor monitor) {
        this.eventService = eventService;
        this.monitor = monitor;
        xmlFactory = XMLInputFactory.newInstance("javax.xml.stream.XMLInputFactory", getClass().getClassLoader());
    }

    /**
     * Processes a query message.
     */
    public int processQuery(ResolverQueryMsg queryMessage) {
        try {
            String message = queryMessage.getQuery();
            QName messageType = getType(message);
            // FIXME we should chnage the transport serialization so we do not need to reposition the stream to the top
            // of the body
            Reader reader = new StringReader(message);
            XMLStreamReader xmlReader = xmlFactory.createXMLStreamReader(reader);
            eventService.publish(messageType, xmlReader);
            return ResolverService.OK;
        } catch (Exception e) {
            // log any exceptions
            monitor.error(e);
            throw new JxtaException(e);
        }

    }

    /**
     * Processes a response message.
     */
    public void processResponse(ResolverResponseMsg responseMessage) {
    }

    private QName getType(String message) throws XMLStreamException {
        Reader reader = new StringReader(message);
        XMLStreamReader xmlReader = xmlFactory.createXMLStreamReader(reader);
        xmlReader.nextTag();
        return xmlReader.getName();

    }
}
