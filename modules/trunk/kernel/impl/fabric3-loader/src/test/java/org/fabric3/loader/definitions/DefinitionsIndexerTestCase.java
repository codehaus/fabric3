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
package org.fabric3.loader.definitions;

import java.io.InputStream;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

import org.fabric3.host.Namespaces;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.ResourceElement;
import org.fabric3.spi.contribution.manifest.QNameSymbol;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;

/**
 * @version $Revision$ $Date$
 */
public class DefinitionsIndexerTestCase extends TestCase {
    DefinitionsIndexer loader;
    private XMLStreamReader reader;

    public void testIndex() throws Exception {
        Resource resource = new Resource(null, "foo");
        IntrospectionContext context = new DefaultIntrospectionContext();
        loader.index(resource, reader, context);

        List<ResourceElement<?, ?>> resourceElements = resource.getResourceElements();
        assertNotNull(resourceElements);
        assertEquals(4, resourceElements.size());

        ResourceElement<?, ?> intentResourceElement = resourceElements.get(0);
        QNameSymbol symbol = (QNameSymbol) intentResourceElement.getSymbol();
        assertEquals(new QName(Namespaces.POLICY, "transactional"), symbol.getKey());

        ResourceElement<?, ?> policySetResourceElement = resourceElements.get(1);
        symbol = (QNameSymbol) policySetResourceElement.getSymbol();
        assertEquals(new QName(Namespaces.POLICY, "transactionalPolicy"), symbol.getKey());
    }

    protected void setUp() throws Exception {
        super.setUp();
        loader = new DefinitionsIndexer(null);
        InputStream stream = getClass().getResourceAsStream("definitions.xml");
        reader = XMLInputFactory.newInstance().createXMLStreamReader(stream);
        reader.nextTag();
    }
}