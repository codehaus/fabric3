/*
* Fabric3
* Copyright (c) 2009 Metaform Systems
*
* Fabric3 is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as
* published by the Free Software Foundation, either version 3 of
* the License, or (at your option) any later version, with the
* following exception:
*
* Linking this software statically or dynamically with other
* modules is making a combined work based on this software.
* Thus, the terms and conditions of the GNU General Public
* License cover the whole combination.
*
* As a special exception, the copyright holders of this software
* give you permission to link this software with independent
* modules to produce an executable, regardless of the license
* terms of these independent modules, and to copy and distribute
* the resulting executable under terms of your choice, provided
* that you also meet, for each linked independent module, the
* terms and conditions of the license of that module. An
* independent module is a module which is not derived from or
* based on this software. If you modify this software, you may
* extend this exception to your version of the software, but
* you are not obligated to do so. If you do not wish to do so,
* delete this exception statement from your version.
*
* Fabric3 is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty
* of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU General Public License for more details.
*
* You should have received a copy of the
* GNU General Public License along with Fabric3.
* If not, see <http://www.gnu.org/licenses/>.
*/
package org.fabric3.hessian.format;

import java.io.Serializable;
import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.fabric3.spi.binding.format.ParameterEncoder;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.MessageImpl;

/**
 * @version $Rev$ $Date$
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
