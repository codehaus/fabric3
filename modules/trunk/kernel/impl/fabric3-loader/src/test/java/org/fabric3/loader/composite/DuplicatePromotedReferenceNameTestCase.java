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
 * --- Original Apache License ---
 *
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
package org.fabric3.loader.composite;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import static org.oasisopen.sca.Constants.SCA_NS;

import org.fabric3.host.contribution.ArtifactValidationFailure;
import org.fabric3.host.contribution.ValidationFailure;
import org.fabric3.model.type.component.ComponentType;
import org.fabric3.model.type.component.CompositeReference;
import org.fabric3.model.type.component.Implementation;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.LoaderException;
import org.fabric3.spi.introspection.xml.LoaderHelper;
import org.fabric3.spi.introspection.xml.LoaderRegistry;
import org.fabric3.spi.introspection.xml.TypeLoader;

/**
 * @version $Rev$ $Date$
 */
public class DuplicatePromotedReferenceNameTestCase extends TestCase {
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
        loader.load(reader, ctx);
        ValidationFailure failure = ctx.getErrors().get(0);
        assertTrue(failure instanceof ArtifactValidationFailure);
        ArtifactValidationFailure artifactFailure = (ArtifactValidationFailure) failure;
        boolean found = false;
        for (ValidationFailure entry : artifactFailure.getFailures()) {
            if (entry instanceof DuplicatePromotedReferenceName) {
                found = true;
                break;
            }
        }
        assertTrue(found);

    }

    protected void setUp() throws Exception {
        super.setUp();
        TypeLoader<CompositeReference> refLoader = createReferenceLoader();
        LoaderRegistry registry = createRegistry();
        LoaderHelper helper = EasyMock.createNiceMock(LoaderHelper.class);
        EasyMock.replay(helper);
        loader = new CompositeLoader(registry, null, null, null, refLoader, null, null, helper);
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
    private <T> TypeLoader<CompositeReference> createReferenceLoader()
            throws XMLStreamException, LoaderException {
        TypeLoader loader = EasyMock.createMock(TypeLoader.class);
        List<URI> uris = new ArrayList<URI>();
        uris.add(URI.create("ref"));
        CompositeReference value = new CompositeReference(REF_NAME, uris);
        EasyMock.expect(loader.load(EasyMock.isA(XMLStreamReader.class),
                                    EasyMock.isA(IntrospectionContext.class))).andReturn(value).times(2);
        EasyMock.replay(loader);
        return (TypeLoader<CompositeReference>) loader;
    }

    private Implementation createImpl() {
        Implementation<ComponentType> impl = new Implementation<ComponentType>() {
            public QName getType() {
                return null;
            }
        };
        ComponentType type = new ComponentType();
        List<URI> uris = new ArrayList<URI>();
        uris.add(URI.create("ref"));
        CompositeReference reference = new CompositeReference(REF_NAME, uris);

        type.add(reference);
        impl.setComponentType(type);
        return impl;
    }

    private XMLStreamReader createReader() throws XMLStreamException {
        Location location = EasyMock.createNiceMock(Location.class);
        EasyMock.replay(location);
        XMLStreamReader reader = EasyMock.createMock(XMLStreamReader.class);
        EasyMock.expect(reader.getNamespaceContext()).andStubReturn(null);
        EasyMock.expect(reader.getAttributeCount()).andReturn(0);
        EasyMock.expect(reader.getAttributeValue(null, "name")).andReturn("composite");
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
        EasyMock.expect(reader.getLocation()).andReturn(location).anyTimes();
        EasyMock.expect(reader.next()).andReturn(2);
        EasyMock.expect(reader.getName()).andReturn(new QName(SCA_NS, "composite"));
        EasyMock.replay(reader);
        return reader;
    }


}