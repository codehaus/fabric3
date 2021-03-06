/*
 * Fabric3
 * Copyright (c) 2009-2011 Metaform Systems
 *
 * Fabric3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version, with the
 * following exception:
 *
 * Linking this software statically or dynamically with other
 * modules is making a combined work based on this software.
 * Thus, the terms and conditions of the GNU General Public
 * License cover the whole combination.
 *
 * As a special exception, the copyright holders of this software
 * give you permission to link this software with independent
 * modules to produce an executable, regardless of the license
 * terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided
 * that you also meet, for each linked independent module, the
 * terms and conditions of the license of that module. An
 * independent module is a module which is not derived from or
 * based on this software. If you modify this software, you may
 * extend this exception to your version of the software, but
 * you are not obligated to do so. If you do not wish to do so,
 * delete this exception statement from your version.
 *
 * Fabric3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the
 * GNU General Public License along with Fabric3.
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.binding.jms.runtime;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;

import org.fabric3.spi.channel.EventStreamHandler;

/**
 * Listens for requests sent to a destination and dispatches to a channel.
 *
 * @version $Revision$ $Date$
 */
public class EventStreamListener implements MessageListener {
    private ClassLoader cl;
    private ListenerMonitor monitor;
    private EventStreamHandler handler;

    public EventStreamListener(ClassLoader cl, EventStreamHandler handler, ListenerMonitor monitor) {
        this.cl = cl;
        this.handler = handler;
        this.monitor = monitor;
    }

    public void onMessage(Message request) {
        ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
        try {
            // set the TCCL to the target service classloader
            Thread.currentThread().setContextClassLoader(cl);
            if (request instanceof ObjectMessage) {
                ObjectMessage message = (ObjectMessage) request;
                handler.handle(message.getObject());
            } else if (request instanceof TextMessage) {
                TextMessage message = (TextMessage) request;
                handler.handle(new Object[]{message.getText()});
            } else {
                String type = request.getClass().getName();
                monitor.invalidMessageType(type);
            }
        } catch (JMSException e) {
            // TODO This could be a temporary error and should be sent to a dead letter queue. For now, just log the error.
            monitor.redeliveryError(e);
        } finally {
            Thread.currentThread().setContextClassLoader(oldCl);
        }
    }


}