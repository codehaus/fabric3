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
package org.fabric3.jaxb.runtime;

import javax.xml.bind.JAXBContext;

import junit.framework.TestCase;

import org.fabric3.jaxb.runtime.impl.JAXB2XmlTransformer;
import org.fabric3.jaxb.runtime.impl.TransformingInterceptor;
import org.fabric3.jaxb.runtime.impl.Xml2JAXBTransformer;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.MessageImpl;
import org.fabric3.spi.wire.Interceptor;

/**
 * @version $Revision$ $Date$
 */
public class TransformingInterceptorTestCase extends TestCase {
    private TransformingInterceptor<Object, String> interceptor;

    public void testInvokeServiceAndReturn() throws Exception {
        Interceptor mock = new MockEchoInterceptor();
        interceptor.setNext(mock);
        Foo foo = new Foo();
        Message msg = new MessageImpl();
        msg.setBody(new Object[]{foo});
        Message ret = interceptor.invoke(msg);
        assertTrue(ret.getBody() instanceof Foo);
    }

    public void testInvokeServiceOneWay() throws Exception {
        Interceptor mock = new MockOneWayInterceptor();
        interceptor.setNext(mock);
        Foo foo = new Foo();
        Message msg = new MessageImpl();
        msg.setBody(new Object[]{foo});
        Message ret = interceptor.invoke(msg);
        assertNull(ret.getBody());
    }

    protected void setUp() throws Exception {
        super.setUp();
        JAXBContext context = JAXBContext.newInstance(Foo.class);
        JAXB2XmlTransformer inTransformer = new JAXB2XmlTransformer(context);
        Xml2JAXBTransformer outTransformer = new Xml2JAXBTransformer(context);
        interceptor = new TransformingInterceptor<Object, String>(inTransformer, outTransformer, getClass().getClassLoader());
    }

    private class MockEchoInterceptor implements Interceptor {
        public Message invoke(Message msg) {
            Object body = msg.getBody();
            Object payload = ((Object[]) body)[0];
            assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><foo/>", payload);
            // echo the results
            msg.setBody(payload);
            return msg;
        }

        public void setNext(Interceptor next) {

        }

        public Interceptor getNext() {
            return null;
        }
    }

    private class MockOneWayInterceptor implements Interceptor {
        public Message invoke(Message msg) {
            Object body = msg.getBody();
            Object payload = ((Object[]) body)[0];
            assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><foo/>", payload);
            msg.setBody(null);
            return msg;
        }

        public void setNext(Interceptor next) {

        }

        public Interceptor getNext() {
            return null;
        }
    }
}
