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
package org.fabric3.hessian.format;

import java.io.Serializable;
import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.fabric3.spi.binding.format.ParameterEncoder;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.MessageImpl;

/**
 * @version $Revision$ $Date$
 */
public class HessianParameterEncoderFactoryTestCase extends TestCase {
    private HessianParameterEncoderFactory factory = new HessianParameterEncoderFactory();

    public void testSerializeObjectToString() throws Exception {
        Foo foo = new Foo();
        foo.setName("test");
        ClassLoader loader = getClass().getClassLoader();
        ParameterEncoder encoder = factory.getInstance(null, loader);
        Message message = new MessageImpl();
        message.setBody(new Object[]{foo});
        String serialized = encoder.encodeText(message);
        Foo deserialized = (Foo) encoder.decode("", serialized);
        assertEquals("test", deserialized.getName());
    }

    public void testSerializeObjectToBytes() throws Exception {
        Foo foo = new Foo();
        foo.setName("test");
        ClassLoader loader = getClass().getClassLoader();
        ParameterEncoder encoder = factory.getInstance(null, loader);
        Message message = new MessageImpl();
        message.setBody(new Object[]{foo});
        byte[] serialized = encoder.encodeBytes(message);
        Foo deserialized = (Foo) encoder.decode("", serialized);
        assertEquals("test", deserialized.getName());
    }

    public void testSerializeQName() throws Exception {
        QName name = new QName("foo", "bar");
        ClassLoader loader = getClass().getClassLoader();
        ParameterEncoder encoder = factory.getInstance(null, loader);
        Message message = new MessageImpl();
        message.setBody(new Object[]{name});

        byte[] serialized = encoder.encodeBytes(message);
        QName deserialized = (QName) encoder.decode("", serialized);
        assertEquals("bar", deserialized.getLocalPart());
    }

    public void testSerializeNull() throws Exception {
        ClassLoader loader = getClass().getClassLoader();
        ParameterEncoder encoder = factory.getInstance(null, loader);
        Message message = new MessageImpl();
        byte[] serialized = encoder.encodeBytes(message);
        assertNull(encoder.decode("", serialized));
    }


    private static class Foo implements Serializable {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
