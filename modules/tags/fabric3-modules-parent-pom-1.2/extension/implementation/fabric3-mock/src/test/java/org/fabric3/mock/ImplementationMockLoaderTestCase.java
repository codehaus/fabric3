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
package org.fabric3.mock;

import java.io.InputStream;
import java.util.List;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.spi.introspection.IntrospectionContext;

/**
 * @version $Rev$ $Date$
 */
public class ImplementationMockLoaderTestCase extends TestCase {

    public void testLoad() throws Exception {

        MockComponentTypeLoader componentTypeLoader = EasyMock.createMock(MockComponentTypeLoader.class);
        IntrospectionContext context = EasyMock.createMock(IntrospectionContext.class);

        ImplementationMockLoader loader = new ImplementationMockLoader(componentTypeLoader);

        InputStream stream = getClass().getClassLoader().getResourceAsStream("META-INF/mock.composite");
        XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(stream);

        while (reader.hasNext()) {
            if (reader.next() == XMLStreamConstants.START_ELEMENT && ImplementationMock.IMPLEMENTATION_MOCK.equals(reader.getName())) {
                break;
            }
        }

        ImplementationMock implementationMock = loader.load(reader, context);
        assertNotNull(implementationMock);

        List<String> interfaces = implementationMock.getMockedInterfaces();
        assertEquals(3, interfaces.size());
        assertEquals(Foo.class.getName(), interfaces.get(0));
        assertEquals(Bar.class.getName(), interfaces.get(1));
        assertEquals(Baz.class.getName(), interfaces.get(2));

    }

}
