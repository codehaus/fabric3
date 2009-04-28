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
package org.fabric3.hessian.runtime;

import java.io.ByteArrayOutputStream;

import com.caucho.hessian.io.Hessian2Output;
import com.caucho.hessian.io.SerializerFactory;
import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.hessian.provision.Encoding;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.MessageImpl;
import org.fabric3.spi.util.Base64;
import org.fabric3.spi.wire.Interceptor;

/**
 * @version $Revision$ $Date$
 */
public class HessianReferenceInterceptorTestCase extends TestCase {
    private HessianReferenceInterceptor interceptor;

    public void testVoid() throws Exception {

        Interceptor next = EasyMock.createMock(Interceptor.class);
        EasyMock.expect(next.invoke(EasyMock.isA(Message.class))).andReturn(new MessageImpl());
        EasyMock.replay(next);
        interceptor.setNext(next);
        interceptor.invoke(new MessageImpl());
        EasyMock.verify(next);
    }

    @SuppressWarnings({"ThrowableInstanceNeverThrown"})
    public void testAsciiException() throws Exception {
        Interceptor next = EasyMock.createMock(Interceptor.class);

        Message ret = createReturnContent(new IllegalArgumentException());
        EasyMock.expect(next.invoke(EasyMock.isA(Message.class))).andReturn(ret);
        EasyMock.replay(next);
        interceptor.setNext(next);

        Message msg = interceptor.invoke(new MessageImpl());
        assertTrue(msg.getBody() instanceof IllegalArgumentException);
        EasyMock.verify(next);
    }

    public void testAsciiString() throws Exception {

        Interceptor next = EasyMock.createMock(Interceptor.class);
        Message msg = createReturnContent("return");
        EasyMock.expect(next.invoke(EasyMock.isA(Message.class))).andReturn(msg);
        EasyMock.replay(next);
        interceptor.setNext(next);
        MessageImpl input = new MessageImpl();
        input.setBody(new Object[]{"input"});
        Message ret = interceptor.invoke(input);
        assertEquals("return", ret.getBody());
        EasyMock.verify(next);
    }

    public void testAsciiPrimitive() throws Exception {
        Interceptor next = EasyMock.createMock(Interceptor.class);
        Message msg = createReturnContent(1);
        EasyMock.expect(next.invoke(EasyMock.isA(Message.class))).andReturn(msg);
        EasyMock.replay(next);
        interceptor.setNext(next);
        MessageImpl input = new MessageImpl();
        input.setBody(new Object[]{1});
        Message ret = interceptor.invoke(input);
        assertEquals(1, ret.getBody());
        EasyMock.verify(next);
    }


    private Message createReturnContent(Object contents) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Message msg = new MessageImpl();
        Hessian2Output out = new Hessian2Output(bos);
        out.startReply();
        if (contents instanceof Exception) {
            out.writeFault("400", "Exception", contents);
            out.completeReply();
            out.close();
            byte[] data = bos.toByteArray();
            msg.setBodyWithFault(Base64.encode(data));
        } else {
            out.writeObject(contents);
            out.completeReply();
            out.close();
            byte[] data = bos.toByteArray();
            msg.setBody(Base64.encode(data));
        }
        return msg;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        SerializerFactory factory = new SerializerFactory();
        interceptor = new HessianReferenceInterceptor(Encoding.ASCII, factory, getClass().getClassLoader());

    }

}
