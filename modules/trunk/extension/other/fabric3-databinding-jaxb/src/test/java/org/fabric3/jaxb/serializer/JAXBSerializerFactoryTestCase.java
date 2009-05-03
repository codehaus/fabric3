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
package org.fabric3.jaxb.serializer;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.fabric3.spi.services.serializer.Serializer;

/**
 * @version $Revision$ $Date$
 */
public class JAXBSerializerFactoryTestCase extends TestCase {

    public void testSerializeDeserialize() throws Exception {
        JAXBSerializerFactory factory = new JAXBSerializerFactory();
        Set<Class<?>> types = new HashSet<Class<?>>();
        types.add(Foo.class);
        Serializer serializer = factory.getInstance(types, Collections.<Class<?>>emptySet());
        String serialized = serializer.serialize(String.class, new Foo());
        assertNotNull(serializer.deserialize(Foo.class, serialized));
    }

}
