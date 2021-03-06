/*
 * Fabric3
 * Copyright � 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the �License�), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an �as is� basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.loader.definitions;

import java.io.InputStream;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

import org.fabric3.spi.services.contribution.QNameSymbol;
import org.fabric3.spi.services.contribution.Resource;
import org.fabric3.spi.services.contribution.ResourceElement;
import org.fabric3.scdl.ValidationContext;
import org.fabric3.scdl.DefaultValidationContext;

/**
 * @version $Revision$ $Date$
 */
public class DefinitionsIndexerTestCase extends TestCase {
    DefinitionsIndexer loader;
    private XMLStreamReader reader;

    public void testIndex() throws Exception {
        Resource resource = new Resource(null, "foo");
        ValidationContext context = new DefaultValidationContext();
        loader.index(resource, reader, context);

        List<ResourceElement<?, ?>> resourceElements = resource.getResourceElements();
        assertNotNull(resourceElements);
        assertEquals(4, resourceElements.size());

        ResourceElement<?, ?> intentResourceElement = resourceElements.get(0);
        QNameSymbol symbol = (QNameSymbol) intentResourceElement.getSymbol();
        assertEquals(new QName("http://fabric3.org/xmlns/sca/2.0-alpha", "transactional"), symbol.getKey());

        ResourceElement<?, ?> policySetResourceElement = resourceElements.get(1);
        symbol = (QNameSymbol) policySetResourceElement.getSymbol();
        assertEquals(new QName("http://fabric3.org/xmlns/sca/2.0-alpha", "transactionalPolicy"), symbol.getKey());
    }

    protected void setUp() throws Exception {
        super.setUp();
        loader = new DefinitionsIndexer(null);
        InputStream stream = getClass().getResourceAsStream("definitions.xml");
        reader = XMLInputFactory.newInstance().createXMLStreamReader(stream);
        reader.nextTag();
    }
}