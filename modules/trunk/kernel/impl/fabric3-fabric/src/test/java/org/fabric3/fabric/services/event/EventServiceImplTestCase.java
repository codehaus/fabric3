/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.fabric.services.event;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.spi.services.event.EventService;
import org.fabric3.spi.services.event.Fabric3Event;
import org.fabric3.spi.services.event.Fabric3EventListener;

/**
 * @version $Rev$ $Date$
 */
public class EventServiceImplTestCase extends TestCase {
    private EventService service;
    private Fabric3EventListener<MockEvent> listener;

    public void testSubscribeUnsubscribe() throws Exception {
        service.subscribe(MockEvent.class, listener);
        service.publish(new MockEvent());
        service.unsubscribe(MockEvent.class, listener);
        service.publish(new MockEvent());
        EasyMock.verify(listener);
    }


    @SuppressWarnings({"unchecked"})
    protected void setUp() throws Exception {
        super.setUp();
        service = new EventServiceImpl();
        listener = EasyMock.createStrictMock(Fabric3EventListener.class);
        listener.onEvent(EasyMock.isA(MockEvent.class));
        EasyMock.replay(listener);
    }

    private class MockEvent implements Fabric3Event {

    }
}
