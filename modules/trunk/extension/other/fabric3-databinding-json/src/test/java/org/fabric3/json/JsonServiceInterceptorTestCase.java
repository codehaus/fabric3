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
package org.fabric3.json;

import java.io.StringWriter;

import junit.framework.TestCase;
import org.codehaus.jackson.map.ObjectMapper;
import org.easymock.EasyMock;

import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.MessageImpl;
import org.fabric3.spi.wire.Interceptor;
import org.fabric3.json.runtime.JsonServiceInterceptor;

/**
 * @version $Revision$ $Date$
 */
public class JsonServiceInterceptorTestCase extends TestCase {

    public void testVoid() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        JsonServiceInterceptor interceptor = new JsonServiceInterceptor(mapper,  Void.class);
        Interceptor next = EasyMock.createMock(Interceptor.class);
        EasyMock.expect(next.invoke(EasyMock.isA(Message.class))).andReturn(new MessageImpl());
        EasyMock.replay(next);
        interceptor.setNext(next);
        interceptor.invoke(new MessageImpl());
        EasyMock.verify(next);
    }

    public void testException() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        JsonServiceInterceptor interceptor = new JsonServiceInterceptor(mapper, Void.class);
        Interceptor next = EasyMock.createMock(Interceptor.class);
        MessageImpl ret = new MessageImpl();
        IllegalArgumentException exception = new IllegalArgumentException();
        StringWriter writer = new StringWriter();
        mapper.writeValue(writer, exception);
        ret.setBodyWithFault(writer.toString());
        EasyMock.expect(next.invoke(EasyMock.isA(Message.class))).andReturn(ret);
        EasyMock.replay(next);
        interceptor.setNext(next);

        Message msg = interceptor.invoke(new MessageImpl());
        IllegalArgumentException returnException = mapper.readValue((String) msg.getBody(),IllegalArgumentException.class);
        assertNotNull(returnException);
        EasyMock.verify(next);
    }

    public void testString() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        JsonServiceInterceptor interceptor = new JsonServiceInterceptor(mapper, String.class);
        Interceptor next = EasyMock.createMock(Interceptor.class);
        MessageImpl msg = new MessageImpl();
        msg.setBody("return");
        EasyMock.expect(next.invoke(EasyMock.isA(Message.class))).andReturn(msg);
        EasyMock.replay(next);
        interceptor.setNext(next);
        MessageImpl input = new MessageImpl();
        input.setBody("\"input\"");
        Message ret = interceptor.invoke(input);
        assertEquals("\"return\"", ret.getBody());
        EasyMock.verify(next);
    }

    public void testPrimitive() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        JsonServiceInterceptor interceptor = new JsonServiceInterceptor(mapper, Integer.TYPE);
        Interceptor next = EasyMock.createMock(Interceptor.class);
        MessageImpl msg = new MessageImpl();
        msg.setBody(1);
        EasyMock.expect(next.invoke(EasyMock.isA(Message.class))).andReturn(msg);
        EasyMock.replay(next);
        interceptor.setNext(next);
        MessageImpl input = new MessageImpl();
        input.setBody("\"1\"");
        Message ret = interceptor.invoke(input);
        assertEquals("1", ret.getBody());
        EasyMock.verify(next);
    }



}