  /*
   * Fabric3
   * Copyright (C) 2009 Metaform Systems
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
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

import org.fabric3.host.Namespaces;
import org.fabric3.loader.impl.DefaultLoaderHelper;
import org.fabric3.model.type.definitions.AbstractDefinition;
import org.fabric3.model.type.definitions.Intent;
import org.fabric3.model.type.definitions.PolicySet;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.ResourceElement;
import org.fabric3.spi.contribution.manifest.QNameSymbol;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.LoaderException;
import org.fabric3.spi.introspection.xml.LoaderHelper;
import org.fabric3.spi.introspection.xml.LoaderRegistry;
import org.fabric3.spi.introspection.xml.TypeLoader;

/**
 * @version $Revision$ $Date$
 */
public class DefinitionsLoaderTestCase extends TestCase {

    public static final QName TRANSACTIONAL_QNAME =
            new QName(Namespaces.POLICY, "transactional");
    public static final QName BINDING_QNAME = new QName("http://docs.oasis-open.org/ns/opencsa/sca/200903", "binding");
    public static final QName TRX_POLICY_QNAME =
            new QName(Namespaces.POLICY, "transactionalPolicy");
    public static final QName SERVER_SEC_POLICY =
            new QName(Namespaces.POLICY, "testServerPolicy");
    public static final QName CLIENT_SEC_POLICY =
            new QName(Namespaces.POLICY, "testClientPolicy");

    private DefinitionsLoader loader;
    private Resource resource;
    private XMLStreamReader reader;


    @SuppressWarnings({"unchecked", "deprecation"})
    public void testLoad() throws Exception {

        IntrospectionContext context = new DefaultIntrospectionContext();
        loader.load(reader, null, resource, context, null);

        List<ResourceElement<?, ?>> resourceElements = resource.getResourceElements();
        assertNotNull(resourceElements);
        assertEquals(4, resourceElements.size());

        ResourceElement<QNameSymbol, AbstractDefinition> intentResourceElement =
                (ResourceElement<QNameSymbol, AbstractDefinition>) resourceElements.get(0);
        assertNotNull(intentResourceElement);

        QNameSymbol symbol = intentResourceElement.getSymbol();
        assertEquals(TRANSACTIONAL_QNAME, symbol.getKey());

        Intent intent = (Intent) intentResourceElement.getValue();
        assertNotNull(intent);
        assertEquals(TRANSACTIONAL_QNAME, intent.getName());
        assertTrue(intent.doesConstrain(BINDING_QNAME));
        assertFalse(intent.isProfile());
        assertFalse(intent.isQualified());
        assertNull(intent.getQualifiable());
        assertEquals(0, intent.getRequires().size());

        ResourceElement<QNameSymbol, AbstractDefinition> policySetResourceElement =
                (ResourceElement<QNameSymbol, AbstractDefinition>) resourceElements.get(1);
        assertNotNull(policySetResourceElement);

        symbol = policySetResourceElement.getSymbol();
        assertEquals(TRX_POLICY_QNAME, symbol.getKey());

        PolicySet policySet = (PolicySet) policySetResourceElement.getValue();
        assertEquals(TRX_POLICY_QNAME, policySet.getName());
        assertTrue(policySet.doesProvide(TRANSACTIONAL_QNAME));

        QName extensionName = policySet.getExtensionName();
        assertEquals("interceptor", extensionName.getLocalPart());
        assertEquals(Namespaces.POLICY, extensionName.getNamespaceURI());

    }

    protected void setUp() throws Exception {
        super.setUp();
        // setup loader infrastructure
        LoaderRegistry loaderRegistry = new MockLoaderRegistry();
        loader = new DefinitionsLoader(null, loaderRegistry);
        LoaderHelper helper = new DefaultLoaderHelper();
        IntentLoader intentLoader = new IntentLoader(helper);
        PolicySetLoader policySetLoader = new PolicySetLoader(helper);
        loaderRegistry.registerLoader(DefinitionsLoader.POLICY_SET, policySetLoader);
        loaderRegistry.registerLoader(DefinitionsLoader.INTENT, intentLoader);

        // setup indexed resource
        resource = new Resource(null, "application/xml");
        // setup up indexed resource elements
        ResourceElement<QNameSymbol, ?> element =
                new ResourceElement<QNameSymbol, AbstractDefinition>(new QNameSymbol(TRANSACTIONAL_QNAME));
        resource.addResourceElement(element);
        element =
                new ResourceElement<QNameSymbol, AbstractDefinition>(new QNameSymbol(TRX_POLICY_QNAME));
        resource.addResourceElement(element);
        element =
                new ResourceElement<QNameSymbol, AbstractDefinition>(new QNameSymbol(SERVER_SEC_POLICY));
        resource.addResourceElement(element);
        element =
                new ResourceElement<QNameSymbol, AbstractDefinition>(new QNameSymbol(CLIENT_SEC_POLICY));
        resource.addResourceElement(element);

        // setup reader
        InputStream stream = getClass().getResourceAsStream("definitions.xml");
        reader = XMLInputFactory.newInstance().createXMLStreamReader(stream);
        while (reader.next() != XMLStreamConstants.START_ELEMENT) {
        }

    }

    @SuppressWarnings("deprecation")
    private static class MockLoaderRegistry implements LoaderRegistry {

        private Map<QName, TypeLoader<?>> loaders = new HashMap<QName, TypeLoader<?>>();

        public void registerLoader(QName element, TypeLoader<?> loader) throws IllegalStateException {
            loaders.put(element, loader);
        }

        public void unregisterLoader(QName element) {
        }

        @SuppressWarnings("unchecked")
        public <OUTPUT> OUTPUT load(XMLStreamReader reader, Class<OUTPUT> type, IntrospectionContext context) throws XMLStreamException {
            return (OUTPUT) loaders.get(reader.getName()).load(reader, context);
        }

        public <OUTPUT> OUTPUT load(URL url, Class<OUTPUT> type, IntrospectionContext context) throws LoaderException {
            return null;
        }

    }

}
