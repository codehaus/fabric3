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
package org.fabric3.json.serializer;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.fabric3.spi.binding.serializer.Serializer;

/**
 * @version $Revision$ $Date$
 */
public class JsonSerializerFactoryTestCase extends TestCase {

    public void testSerializeDeserializeString() throws Exception {
        JsonSerializerFactory factory = new JsonSerializerFactory();
        Set<Class<?>> types = new HashSet<Class<?>>();
        types.add(String.class);
        Serializer serializer = factory.getInstance(types, Collections.<Class<?>>emptySet());
        String serialized = serializer.serialize(String.class, "test");
        assertEquals("test", serializer.deserialize(String.class, serialized));
    }

    public void testSerializeDeserializeNull() throws Exception {
        JsonSerializerFactory factory = new JsonSerializerFactory();
        Serializer serializer = factory.getInstance(Collections.<Class<?>>emptySet(), Collections.<Class<?>>emptySet());
        String serialized = serializer.serialize(String.class, null);
        assertNull(serializer.deserialize(Object.class, serialized));
    }

    public void testSerializeDeserializeObject() throws Exception {
        JsonSerializerFactory factory = new JsonSerializerFactory();
        Set<Class<?>> types = new HashSet<Class<?>>();
        types.add(Foo.class);
        Serializer serializer = factory.getInstance(types, Collections.<Class<?>>emptySet());
        Foo foo = new Foo();
        foo.setName("test");
        String serialized = serializer.serialize(String.class, foo);
        Foo deserialzed = (Foo) serializer.deserialize(Object.class, serialized);
        assertEquals("test", deserialzed.getName());
    }

    public void testSerializeDeserializeException() throws Exception {
        JsonSerializerFactory factory = new JsonSerializerFactory();
        Set<Class<?>> faults = new HashSet<Class<?>>();
        faults.add(FooException.class);
        Serializer serializer = factory.getInstance(Collections.<Class<?>>emptySet(), faults);
        FooException fault = new FooException("test");
        String serialized = serializer.serializeFault(String.class, fault);
        Throwable deserialized = serializer.deserializeFault(serialized);

        assertTrue(deserialized instanceof FooException);
    }


    private static class Foo {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    private static class FooException extends Exception {
        private static final long serialVersionUID = 4937174167807498685L;

        public FooException(String message) {
            super(message);
        }
    }
}
