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
package org.fabric3.fabric.services.serializer;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import org.osoa.sca.Conversation;

import org.fabric3.spi.invocation.CallFrame;
import org.fabric3.spi.invocation.ConversationContext;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.MessageImpl;
import org.fabric3.spi.invocation.WorkContext;

/**
 * @version $Revision$ $Date$
 */
public class JDKSerializerTestCase extends TestCase {
    private JDKSerializer serializer = new JDKSerializer();
    private Message message;

    public void testRountTrip() throws Exception {
        byte[] bytes = serializer.serialize(message);
        Message msg = serializer.deserialize(Message.class, bytes);
        assertEquals(1, msg.getWorkContext().getCallFrameStack().size());
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        message = new MessageImpl();
        List<CallFrame> frames = new ArrayList<CallFrame>();
        Conversation conversation = new MockConversation();
        CallFrame frame = new CallFrame("callbackUri", "correlationId", conversation, ConversationContext.PROPAGATE);
        frames.add(frame);
        WorkContext context = new WorkContext();
        context.addCallFrames(frames);
        message.setWorkContext(context);
    }

}
