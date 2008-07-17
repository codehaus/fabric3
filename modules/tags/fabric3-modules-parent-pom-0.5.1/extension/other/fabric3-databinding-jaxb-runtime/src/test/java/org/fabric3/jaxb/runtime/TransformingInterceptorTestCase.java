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
package org.fabric3.jaxb.runtime;

import javax.xml.bind.JAXBContext;

import junit.framework.TestCase;

import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.MessageImpl;
import org.fabric3.spi.wire.Interceptor;
import org.fabric3.jaxb.runtime.impl.JAXB2XmlTransformer;
import org.fabric3.jaxb.runtime.impl.TransformingInterceptor;
import org.fabric3.jaxb.runtime.impl.Xml2JAXBTransformer;

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
