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

import java.net.URI;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import static org.osoa.sca.Constants.SCA_NS;

import org.fabric3.introspection.DefaultIntrospectionContext;
import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.xml.LoaderException;
import org.fabric3.introspection.xml.LoaderHelper;
import org.fabric3.introspection.xml.LoaderRegistry;
import org.fabric3.introspection.xml.TypeLoader;
import org.fabric3.scdl.ComponentType;
import org.fabric3.scdl.Implementation;
import org.fabric3.scdl.Property;
import org.fabric3.scdl.ArtifactValidationFailure;
import org.fabric3.host.contribution.ValidationFailure;

/**
 * @version $Rev$ $Date$
 */
public class DuplicatePropertyTestCase extends TestCase {
    public static final String PROP_NAME = "notThere";

    private CompositeLoader loader;
    private XMLStreamReader reader;
    private IntrospectionContext ctx;

    /**
     * Verifies an exception is thrown if an attempt is made to configure a property twice.
     *
     * @throws Exception on test failure
     */
    public void testDuplicateProperty() throws Exception {
        loader.load(reader, ctx);
        ValidationFailure failure = ctx.getErrors().get(0);
        assertTrue(failure instanceof ArtifactValidationFailure);
        assertTrue(((ArtifactValidationFailure) failure).getFailures().get(0) instanceof DuplicateProperty);
    }

    protected void setUp() throws Exception {
        super.setUp();
        TypeLoader<Property> propLoader = createPropertyLoader();
        LoaderRegistry registry = createRegistry();
        LoaderHelper helper = EasyMock.createNiceMock(LoaderHelper.class);
        EasyMock.replay(helper);
        loader = new CompositeLoader(registry, null, propLoader, null, null, null, null, helper);
        reader = createReader();
        ctx = new DefaultIntrospectionContext(URI.create("parent"), getClass().getClassLoader(), "foo");
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
    private <T> TypeLoader<Property> createPropertyLoader() throws XMLStreamException, LoaderException {
        TypeLoader loader = EasyMock.createMock(TypeLoader.class);
        Property value = new Property(PROP_NAME, null);
        EasyMock.expect(loader.load(EasyMock.isA(XMLStreamReader.class),
                                    EasyMock.isA(IntrospectionContext.class))).andReturn(value).times(2);
        EasyMock.replay(loader);
        return (TypeLoader<Property>) loader;
    }

    private Implementation createImpl() {
        Implementation<ComponentType> impl = new Implementation<ComponentType>() {
            public QName getType() {
                return null;
            }
        };
        ComponentType type = new ComponentType();
        Property property = new Property(PROP_NAME, null);

        type.add(property);
        impl.setComponentType(type);
        return impl;
    }

    private XMLStreamReader createReader() throws XMLStreamException {
        XMLStreamReader reader = EasyMock.createMock(XMLStreamReader.class);
        Location location = EasyMock.createNiceMock(Location.class);
        EasyMock.replay(location);
        EasyMock.expect(reader.getAttributeCount()).andReturn(0);
        EasyMock.expect(reader.getNamespaceContext()).andStubReturn(null);
        EasyMock.expect(reader.getAttributeValue(null, "name")).andReturn("composite");
        EasyMock.expect(reader.getAttributeValue(null, "targetNamespace")).andReturn("http:///somenamepace");
        EasyMock.expect(reader.getAttributeValue(null, "local")).andReturn(null);
        EasyMock.expect(reader.getAttributeValue(null, "constrainingType")).andReturn(null);
        EasyMock.expect(reader.getAttributeValue(null, "autowire")).andReturn("true");

        EasyMock.expect(reader.nextTag()).andReturn(1);
        EasyMock.expect(reader.next()).andReturn(1);
        EasyMock.expect(reader.getName()).andReturn(new QName(SCA_NS, "property"));
        EasyMock.expect(reader.nextTag()).andReturn(1);
        EasyMock.expect(reader.next()).andReturn(1);
        EasyMock.expect(reader.getName()).andReturn(new QName(SCA_NS, "property"));
        EasyMock.expect(reader.getLocation()).andReturn(location);
        EasyMock.expect(reader.next()).andReturn(2);
        EasyMock.expect(reader.getName()).andReturn(new QName(SCA_NS, "composite"));
        EasyMock.replay(reader);
        return reader;
    }


}