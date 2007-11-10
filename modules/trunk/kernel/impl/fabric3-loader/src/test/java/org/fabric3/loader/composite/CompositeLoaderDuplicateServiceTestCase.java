/*
 * See the NOTICE file distributed with this work for information
 * regarding copyright ownership.  This file is licensed
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
package org.fabric3.loader.composite;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import static org.osoa.sca.Constants.SCA_NS;

import org.fabric3.scdl.ComponentType;
import org.fabric3.scdl.CompositeService;
import org.fabric3.scdl.Implementation;
import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.spi.loader.LoaderException;
import org.fabric3.spi.loader.LoaderRegistry;
import org.fabric3.spi.loader.PolicyHelper;
import org.fabric3.spi.loader.StAXElementLoader;

/**
 * @version $Rev$ $Date$
 */
public class CompositeLoaderDuplicateServiceTestCase extends TestCase {
    public static final String SERVICE_NAME = "notThere";

    private CompositeLoader loader;
    private XMLStreamReader reader;
    private LoaderContext ctx;

    /**
     * Verifies an exception is thrown if an attempt is made to configure a service twice.
     *
     * @throws Exception on test failure
     */
    public void testDuplicateService() throws Exception {
        EasyMock.replay(ctx);
        try {
            loader.load(reader, ctx);
        } catch (DuplicateServiceException e) {
            assertEquals(SERVICE_NAME, e.getIdentifier());
        }
    }

    protected void setUp() throws Exception {
        super.setUp();
        StAXElementLoader<CompositeService> serviceLoader = createServiceLoader();
        LoaderRegistry registry = createRegistry();
        PolicyHelper helper = EasyMock.createNiceMock(PolicyHelper.class);
        EasyMock.replay(helper);
        loader = new CompositeLoader(registry, null, null, serviceLoader, null, null, null, helper);
        reader = createReader();
        ctx = EasyMock.createNiceMock(LoaderContext.class);
    }

    private LoaderRegistry createRegistry() throws XMLStreamException, LoaderException {
        LoaderRegistry registry = EasyMock.createMock(LoaderRegistry.class);
        Implementation impl = createImpl();
        EasyMock.expect(registry.load(EasyMock.isA(XMLStreamReader.class),
                                      EasyMock.eq(Implementation.class),
                                      EasyMock.isA(LoaderContext.class))).andReturn(impl);

        EasyMock.replay(registry);
        return registry;
    }

    @SuppressWarnings({"unchecked"})
    private <T> StAXElementLoader<CompositeService> createServiceLoader()
            throws XMLStreamException, LoaderException {
        StAXElementLoader loader = EasyMock.createMock(StAXElementLoader.class);
        CompositeService value = new CompositeService(SERVICE_NAME, null);
        EasyMock.expect(loader.load(EasyMock.isA(XMLStreamReader.class),
                                    EasyMock.isA(LoaderContext.class))).andReturn(value).times(2);
        EasyMock.replay(loader);
        return (StAXElementLoader<CompositeService>) loader;
    }

    private Implementation createImpl() {
        Implementation<ComponentType> impl = new Implementation<ComponentType>() {
            public QName getType() {
                return null;
            }
        };
        ComponentType type = new ComponentType();
        CompositeService service = new CompositeService(SERVICE_NAME, null);

        type.add(service);
        impl.setComponentType(type);
        return impl;
    }

    private XMLStreamReader createReader() throws XMLStreamException {
        XMLStreamReader reader = EasyMock.createMock(XMLStreamReader.class);
        EasyMock.expect(reader.getNamespaceContext()).andStubReturn(null);
        EasyMock.expect(reader.getAttributeValue(null, "name")).andReturn("comppsite");
        EasyMock.expect(reader.getAttributeValue(null, "targetNamespace")).andReturn("http:///somenamepace");
        EasyMock.expect(reader.getAttributeValue(null, "local")).andReturn(null);
        EasyMock.expect(reader.getAttributeValue(null, "constrainingType")).andReturn(null);
        EasyMock.expect(reader.getAttributeValue(null, "autowire")).andReturn("true");

        EasyMock.expect(reader.nextTag()).andReturn(1);
        EasyMock.expect(reader.next()).andReturn(1);
        EasyMock.expect(reader.getName()).andReturn(new QName(SCA_NS, "service"));
        EasyMock.expect(reader.nextTag()).andReturn(1);
        EasyMock.expect(reader.next()).andReturn(1);
        EasyMock.expect(reader.getName()).andReturn(new QName(SCA_NS, "service"));
        EasyMock.replay(reader);
        return reader;
    }


}