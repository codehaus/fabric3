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
package org.fabric3.fabric.binding.format;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.spi.binding.format.EncodeCallback;
import org.fabric3.spi.binding.format.HeaderContext;
import org.fabric3.spi.binding.format.MessageEncoder;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.MessageImpl;

/**
 * @version $Revision$ $Date$
 */
public class JDKMessageEncoderTestCase extends TestCase {

    public void testRountTrip() throws Exception {
        MessageEncoder encoder = new JDKMessageEncoder();

        Message message = new MessageImpl();
        message.setBody(new Object[]{"test"});
        EncodeCallback callback = EasyMock.createNiceMock(EncodeCallback.class);
        HeaderContext context = EasyMock.createNiceMock(HeaderContext.class);
        EasyMock.replay(callback, context);
        byte[] bytes = encoder.encodeBytes("", message, callback);
        Message deserialized = encoder.decode(bytes, context);
        assertEquals("test", ((Object[]) deserialized.getBody())[0]);
    }

    public void testNull() throws Exception {
        MessageEncoder encoder = new JDKMessageEncoder();

        Message message = new MessageImpl();
        EncodeCallback callback = EasyMock.createNiceMock(EncodeCallback.class);
        HeaderContext context = EasyMock.createNiceMock(HeaderContext.class);
        EasyMock.replay(callback, context);
        byte[] bytes = encoder.encodeBytes("", message, callback);
        Message deserialized = encoder.decode(bytes, context);
        assertNull(deserialized.getBody());
    }

}