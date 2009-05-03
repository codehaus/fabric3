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
package org.fabric3.hessian.serializer;

import java.io.Serializable;
import java.util.Collections;

import junit.framework.TestCase;

import org.fabric3.spi.services.serializer.Serializer;

/**
 * @version $Revision$ $Date$
 */
public class HessianSerializerFactoryTestCase extends TestCase {
    private HessianSerializerFactory serializerFactory = new HessianSerializerFactory();

    public void testSerializeObjectToString() throws Exception {
        Foo foo = new Foo();
        foo.setName("test");
        Serializer serializer = serializerFactory.getInstance(Collections.<Class<?>>emptySet(), Collections.<Class<?>>emptySet());
        String serialized = serializer.serialize(String.class, foo);
        Foo deserialized = serializer.deserialize(Foo.class, serialized);
        assertEquals("test", deserialized.getName());
    }

    public void testSerializeObjectToBytes() throws Exception {
        Foo foo = new Foo();
        foo.setName("test");
        Serializer serializer = serializerFactory.getInstance(Collections.<Class<?>>emptySet(), Collections.<Class<?>>emptySet());
        byte[] serialized = serializer.serialize(byte[].class, foo);
        Foo deserialized = serializer.deserialize(Foo.class, serialized);
        assertEquals("test", deserialized.getName());
    }

    public void testSerializeNull() throws Exception {
        Serializer serializer = serializerFactory.getInstance(Collections.<Class<?>>emptySet(), Collections.<Class<?>>emptySet());
        byte[] serialized = serializer.serialize(byte[].class, null);
        assertNull(serializer.deserialize(Foo.class, serialized));
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
