/*
 * Fabric3
 * Copyright (c) 2009-2012 Metaform Systems
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
*/
package org.fabric3.fabric.channel;

import java.net.URI;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.spi.channel.ChannelConnection;
import org.fabric3.spi.channel.EventStream;
import org.fabric3.spi.channel.EventStreamHandler;
import org.fabric3.spi.model.physical.PhysicalEventStreamDefinition;

/**
 * @version $Rev: 8947 $ $Date: 2010-05-02 15:09:45 +0200 (Sun, 02 May 2010) $
 */
public class SyncFanOutHandlerTestCase extends TestCase {

    public void testFanOut() throws Exception {
        SyncFanOutHandler handler = new SyncFanOutHandler();

        Object event = new Object();

        EventStreamHandler handler1 = EasyMock.createMock(EventStreamHandler.class);
        ChannelConnection connection1 = createConnection("handler1", handler1);

        EventStreamHandler handler2 = EasyMock.createMock(EventStreamHandler.class);
        ChannelConnection connection2 = createConnection("handler2", handler2);

        handler.addConnection(URI.create("connection1"), connection1);
        handler.addConnection(URI.create("connection2"), connection2);

        handler1.handle(event);
        handler2.handle(event);

        EasyMock.replay(handler1, handler2);

        handler.handle(event);

        EasyMock.verify(handler1, handler2);
    }

    private ChannelConnection createConnection(String name, EventStreamHandler handler) {
        PhysicalEventStreamDefinition definition = new PhysicalEventStreamDefinition(name);
        EventStream stream1 = new EventStreamImpl(definition);
        stream1.addHandler(handler);
        ChannelConnection connection = new ChannelConnectionImpl();
        connection.addEventStream(stream1);
        return connection;
    }

}