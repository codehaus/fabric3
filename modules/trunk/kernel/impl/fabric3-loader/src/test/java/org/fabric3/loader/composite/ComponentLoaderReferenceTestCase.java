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

import org.fabric3.scdl.ComponentReference;
import org.fabric3.scdl.ComponentType;
import org.fabric3.scdl.Implementation;
import org.fabric3.spi.loader.Loader;
import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.spi.loader.LoaderException;
import org.fabric3.spi.loader.PolicyHelper;
import org.fabric3.spi.loader.StAXElementLoader;

/**
 * @version $Rev$ $Date$
 */
public class ComponentLoaderReferenceTestCase extends TestCase {
    public static final String REF_NAME = "notThere";

    private ComponentLoader loader;
    private XMLStreamReader reader;
    private IntrospectionContext ctx;

    /**
     * Verifies an exception is thrown if an attempt is made to configure a non-existent reference.
     *
     * @throws Exception on test failure
     */
    public void testNoReference() throws Exception {
        EasyMock.replay(ctx);
        try {
            loader.load(reader, ctx);
            fail();
        } catch (ComponentReferenceNotFoundException e) {
            assertEquals(REF_NAME, e.getName());
        }
    }

    protected void setUp() throws Exception {
        super.setUp();
        StAXElementLoader<ComponentReference> referenceLoader = createReferenceLoader();
        Loader registry = createRegistry();
        PolicyHelper helper = EasyMock.createNiceMock(PolicyHelper.class);
        EasyMock.replay(helper);
        loader = new ComponentLoader(registry, null, referenceLoader, null, helper);
        reader = createReader();
        ctx = EasyMock.createNiceMock(IntrospectionContext.class);
    }

    private Loader createRegistry() throws XMLStreamException, LoaderException {
        Loader registry = EasyMock.createMock(Loader.class);
        Implementation impl = createImpl();
        EasyMock.expect(registry.load(EasyMock.isA(XMLStreamReader.class),
                                      EasyMock.eq(Implementation.class),
                                      EasyMock.isA(IntrospectionContext.class))).andReturn(impl);

        EasyMock.replay(registry);
        return registry;
    }

    @SuppressWarnings({"unchecked"})
    private StAXElementLoader<ComponentReference> createReferenceLoader() throws XMLStreamException, LoaderException {
        StAXElementLoader<ComponentReference> referenceLoader = EasyMock.createMock(StAXElementLoader.class);
        ComponentReference reference = new ComponentReference(REF_NAME);
        EasyMock.expect(referenceLoader.load(EasyMock.isA(XMLStreamReader.class),
                                             EasyMock.isA(IntrospectionContext.class))).andReturn(reference);
        EasyMock.replay(referenceLoader);
        return referenceLoader;
    }

    private Implementation createImpl() {
        Implementation<ComponentType> impl = new Implementation<ComponentType>() {
            public QName getType() {
                return null;
            }
        };
        impl.setComponentType(new ComponentType());
        return impl;
    }

    private XMLStreamReader createReader() throws XMLStreamException {
        XMLStreamReader reader = EasyMock.createMock(XMLStreamReader.class);
        EasyMock.expect(reader.getAttributeValue(null, "name")).andReturn("component");
        EasyMock.expect(reader.getAttributeValue(null, "autowire")).andReturn(null);
        EasyMock.expect(reader.getAttributeValue(null, "runtimeId")).andReturn(null);
        EasyMock.expect(reader.getAttributeValue(null, "initLevel")).andReturn(null);
        EasyMock.expect(reader.getAttributeValue(EasyMock.isA(String.class), EasyMock.eq("key"))).andReturn(null);
        EasyMock.expect(reader.nextTag()).andReturn(1);
        EasyMock.expect(reader.next()).andReturn(1);
        EasyMock.expect(reader.getName()).andReturn(new QName(SCA_NS, "reference"));
        EasyMock.replay(reader);
        return reader;
    }


}
