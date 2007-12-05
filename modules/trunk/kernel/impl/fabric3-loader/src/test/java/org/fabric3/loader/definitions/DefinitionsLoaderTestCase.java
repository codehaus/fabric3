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

import org.fabric3.scdl.definitions.AbstractDefinition;
import org.fabric3.scdl.definitions.Intent;
import org.fabric3.scdl.definitions.PolicySet;
import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.spi.loader.LoaderException;
import org.fabric3.spi.loader.LoaderRegistry;
import org.fabric3.spi.loader.StAXElementLoader;
import org.fabric3.spi.services.contribution.QNameSymbol;
import org.fabric3.spi.services.contribution.Resource;
import org.fabric3.spi.services.contribution.ResourceElement;

/**
 * @version $Revision$ $Date$
 */
public class DefinitionsLoaderTestCase extends TestCase {
    public static final QName TRANSACTIONAL_QNAME =
            new QName("http://fabric3.org/xmlns/sca/2.0-alpha", "transactional");
    public static final QName BINDING_QNAME = new QName("http://www.osoa.org/xmlns/sca/1.0", "binding");
    public static final QName TRX_POLICY_QNAME =
            new QName("http://fabric3.org/xmlns/sca/2.0-alpha", "transactionalPolicy");
    private DefinitionsLoader loader;
    private Resource resource;
    private XMLStreamReader reader;


    @SuppressWarnings({"unchecked", "deprecation"})
    public void testLoad() throws Exception {

        loader.load(reader, null, resource, null);

        List<ResourceElement<?, ?>> resourceElements = resource.getResourceElements();
        assertNotNull(resourceElements);
        assertEquals(2, resourceElements.size());

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
        assertEquals("http://fabric3.org/xmlns/sca/2.0-alpha", extensionName.getNamespaceURI());

    }

    protected void setUp() throws Exception {
        super.setUp();
        // setup loader infrastructure
        LoaderRegistry loaderRegistry = new MockLoaderRegistry();
        loader = new DefinitionsLoader(null, loaderRegistry);
        IntentLoader intentLoader = new IntentLoader();
        PolicySetLoader policySetLoader = new PolicySetLoader(loaderRegistry);
        policySetLoader.init();
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

        // setup reader
        InputStream stream = getClass().getResourceAsStream("definitions.xml");
        reader = XMLInputFactory.newInstance().createXMLStreamReader(stream);
        while (reader.next() != XMLStreamConstants.START_ELEMENT) {
        }

    }

    @SuppressWarnings("deprecation")
    private static class MockLoaderRegistry implements LoaderRegistry {

        private Map<QName, StAXElementLoader<?>> loaders = new HashMap<QName, StAXElementLoader<?>>();

        public void registerLoader(QName element, StAXElementLoader<?> loader)
                throws IllegalStateException {
            loaders.put(element, loader);
        }

        public void unregisterLoader(QName element) {
        }

        @SuppressWarnings("unchecked")
        public <OUTPUT> OUTPUT load(XMLStreamReader reader, Class<OUTPUT> type,
                                    LoaderContext context) throws XMLStreamException,
                LoaderException {
            return (OUTPUT) loaders.get(reader.getName()).load(reader, context);
        }

        public <OUTPUT> OUTPUT load(URL url, Class<OUTPUT> type,
                                    LoaderContext context) throws LoaderException {
            // TODO Auto-generated method stub
            return null;
        }

    }

}
