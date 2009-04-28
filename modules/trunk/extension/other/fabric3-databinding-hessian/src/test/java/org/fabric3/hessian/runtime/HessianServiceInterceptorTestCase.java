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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import com.caucho.hessian.io.Hessian2Input;
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
public class HessianServiceInterceptorTestCase extends TestCase {
    private HessianServiceInterceptor interceptor;

    public void testVoid() throws Exception {
        Interceptor next = EasyMock.createMock(Interceptor.class);
        EasyMock.expect(next.invoke(EasyMock.isA(Message.class))).andReturn(new MessageImpl());
        EasyMock.replay(next);
        interceptor.setNext(next);
        interceptor.invoke(new MessageImpl());
        EasyMock.verify(next);
    }

    @SuppressWarnings({"ThrowableInstanceNeverThrown"})
    public void testException() throws Exception {

        Interceptor next = EasyMock.createMock(Interceptor.class);
        MessageImpl ret = new MessageImpl();
        IllegalArgumentException exception = new IllegalArgumentException();

        ret.setBodyWithFault(exception);
        EasyMock.expect(next.invoke(EasyMock.isA(Message.class))).andReturn(ret);
        EasyMock.replay(next);
        interceptor.setNext(next);

        Message msg = interceptor.invoke(new MessageImpl());
        Object val = readValue(msg);
        assertTrue(val instanceof IllegalArgumentException);
        EasyMock.verify(next);
    }


    public void testString() throws Exception {

        Interceptor next = EasyMock.createMock(Interceptor.class);
        MessageImpl msg = new MessageImpl();
        msg.setBody("return");
        EasyMock.expect(next.invoke(EasyMock.isA(Message.class))).andReturn(msg);
        EasyMock.replay(next);
        interceptor.setNext(next);
        Message input = createInputContent(new Object[]{"input"});
        Message ret = interceptor.invoke(input);

        Object val = readValue(ret);
        assertEquals("return", val);
        EasyMock.verify(next);
    }

    public void testPrimitive() throws Exception {
        Interceptor next = EasyMock.createMock(Interceptor.class);
        MessageImpl msg = new MessageImpl();
        msg.setBody(1);
        EasyMock.expect(next.invoke(EasyMock.isA(Message.class))).andReturn(msg);
        EasyMock.replay(next);
        interceptor.setNext(next);
        Message input = createInputContent(new Object[]{1});
        Message ret = interceptor.invoke(input);
        Object val = readValue(ret);
        assertEquals(1, val);
        EasyMock.verify(next);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        SerializerFactory factory = new SerializerFactory();
        interceptor = new HessianServiceInterceptor(Encoding.ASCII, factory, getClass().getClassLoader());

    }

    private Message createInputContent(Object contents) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Message msg = new MessageImpl();
        Hessian2Output out = new Hessian2Output(bos);
        out.startMessage();
        out.writeObject(contents);
        out.completeMessage();
        out.close();
        byte[] data = bos.toByteArray();
        msg.setBody(Base64.encode(data));
        return msg;
    }

    private Object readValue(Message msg) throws Exception {
        String body = (String) msg.getBody();
        ByteArrayInputStream bin = new ByteArrayInputStream(Base64.decode(body));
        Hessian2Input in = new Hessian2Input(bin);
        Object val;
        try {
            val = in.readReply(null);
        } catch (Throwable throwable) {
            val = throwable;
        }
        in.close();
        bin.close();
        return val;

    }

}