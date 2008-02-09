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
import org.fabric3.scdl.CompositeReference;
import org.fabric3.scdl.Implementation;
import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.spi.loader.LoaderException;
import org.fabric3.spi.loader.LoaderRegistry;
import org.fabric3.spi.loader.PolicyHelper;
import org.fabric3.spi.loader.StAXElementLoader;

/**
 * @version $Rev$ $Date$
 */
public class CompositeLoaderDuplicateReferenceTestCase extends TestCase {
    public static final String REF_NAME = "notThere";

    private CompositeLoader loader;
    private XMLStreamReader reader;
    private IntrospectionContext ctx;

    /**
     * Verifies an exception is thrown if an attempt is made to configure a reference twice.
     *
     * @throws Exception on test failure
     */
    public void testDuplicateReference() throws Exception {
        EasyMock.replay(ctx);
        try {
            loader.load(reader, ctx);
        } catch (DuplicateReferenceException e) {
            assertEquals(REF_NAME, e.getIdentifier());
        }
    }

    protected void setUp() throws Exception {
        super.setUp();
        StAXElementLoader<CompositeReference> refLoader = createReferenceLoader();
        LoaderRegistry registry = createRegistry();
        PolicyHelper helper = EasyMock.createNiceMock(PolicyHelper.class);
        EasyMock.replay(helper);
        loader = new CompositeLoader(registry, null, null, null, refLoader, null, null, helper);
        reader = createReader();
        ctx = EasyMock.createNiceMock(IntrospectionContext.class);
    }

    private LoaderRegistry createRegistry() throws XMLStreamException, LoaderException {
        LoaderRegistry registry = EasyMock.createMock(LoaderRegistry.class);
        Implementation impl = createImpl();
        EasyMock.expect(registry.load(EasyMock.isA(XMLStreamReader.class),
                                      EasyMock.eq(Implementation.class),
                                      EasyMock.isA(IntrospectionContext.class))).andReturn(impl);

        EasyMock.replay(registry);
        return registry;
    }

    @SuppressWarnings({"unchecked"})
    private <T> StAXElementLoader<CompositeReference> createReferenceLoader()
            throws XMLStreamException, LoaderException {
        StAXElementLoader loader = EasyMock.createMock(StAXElementLoader.class);
        CompositeReference value = new CompositeReference(REF_NAME, null);
        EasyMock.expect(loader.load(EasyMock.isA(XMLStreamReader.class),
                                    EasyMock.isA(IntrospectionContext.class))).andReturn(value).times(2);
        EasyMock.replay(loader);
        return (StAXElementLoader<CompositeReference>) loader;
    }

    private Implementation createImpl() {
        Implementation<ComponentType> impl = new Implementation<ComponentType>() {
            public QName getType() {
                return null;
            }
        };
        ComponentType type = new ComponentType();
        CompositeReference reference = new CompositeReference(REF_NAME, null);

        type.add(reference);
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
        EasyMock.expect(reader.getName()).andReturn(new QName(SCA_NS, "reference"));
        EasyMock.expect(reader.nextTag()).andReturn(1);
        EasyMock.expect(reader.next()).andReturn(1);
        EasyMock.expect(reader.getName()).andReturn(new QName(SCA_NS, "reference"));
        EasyMock.replay(reader);
        return reader;
    }


}