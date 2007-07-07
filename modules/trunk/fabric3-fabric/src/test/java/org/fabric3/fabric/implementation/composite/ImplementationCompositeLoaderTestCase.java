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
package org.fabric3.fabric.implementation.composite;

import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.namespace.NamespaceContext;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import static org.osoa.sca.Constants.SCA_NS;

import org.fabric3.spi.deployer.CompositeClassLoader;
import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.spi.loader.LoaderException;
import org.fabric3.spi.model.type.CompositeImplementation;
import org.fabric3.spi.services.artifact.Artifact;
import org.fabric3.spi.services.artifact.ArtifactRepository;

import junit.framework.TestCase;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reportMatcher;
import static org.easymock.EasyMock.verify;
import org.easymock.IArgumentMatcher;

/**
 * @version $Rev$ $Date$
 */
public class ImplementationCompositeLoaderTestCase extends TestCase {
    private static final QName IMPLEMENTATION_COMPOSITE = new QName(SCA_NS, "implementation.composite");

    private ClassLoader cl;
    private ImplementationCompositeLoader loader;
    private XMLStreamReader reader;
    private NamespaceContext namespaceContext;
    private LoaderContext context;
    private ArtifactRepository artifactRepository;

    public void testName() throws LoaderException, XMLStreamException, MalformedURLException {
        String name = "foo";
        expect(reader.getName()).andReturn(IMPLEMENTATION_COMPOSITE);
        expect(reader.getAttributeValue(null, "name")).andReturn(name);
        expect(reader.getNamespaceContext()).andReturn(namespaceContext);
        expect(reader.next()).andReturn(END_ELEMENT);
        replay(reader, namespaceContext, context, artifactRepository);

        CompositeImplementation impl = loader.load(reader, context);
        verify(reader, namespaceContext, context, artifactRepository);
        assertEquals(new QName(name), impl.getName());
    }

    public void testWithArtifact() throws LoaderException, XMLStreamException, MalformedURLException {
        String name = "foo";
        expect(reader.getName()).andReturn(IMPLEMENTATION_COMPOSITE);
        expect(reader.getAttributeValue(null, "name")).andReturn(name);
        expect(reader.getNamespaceContext()).andReturn(namespaceContext);
        expect(reader.next()).andReturn(END_ELEMENT);
        replay(reader, namespaceContext, context, artifactRepository);

        CompositeImplementation impl = loader.load(reader, context);
        verify(reader, namespaceContext, context, artifactRepository);
        assertEquals(new QName(name), impl.getName());
    }

    public void testWithScdlLocation() throws LoaderException, XMLStreamException, MalformedURLException {
        String name = "foo";
        expect(reader.getName()).andReturn(IMPLEMENTATION_COMPOSITE);
        expect(reader.getAttributeValue(null, "name")).andReturn(name);
        expect(reader.getNamespaceContext()).andReturn(namespaceContext);
        expect(reader.next()).andReturn(END_ELEMENT);
        replay(reader, namespaceContext, context, artifactRepository);

        CompositeImplementation impl = loader.load(reader, context);
        verify(reader, namespaceContext, context, artifactRepository);
        assertEquals(new QName(name), impl.getName());
    }

    public void testWithJarLocation() throws LoaderException, XMLStreamException, MalformedURLException {
        String name = "foo";
        expect(reader.getName()).andReturn(IMPLEMENTATION_COMPOSITE);
        expect(reader.getAttributeValue(null, "name")).andReturn(name);
        expect(reader.getNamespaceContext()).andReturn(namespaceContext);
        expect(reader.next()).andReturn(END_ELEMENT);
        replay(reader, namespaceContext, context, artifactRepository);

        CompositeImplementation impl = loader.load(reader, context);
        verify(reader, namespaceContext, context, artifactRepository);
        assertEquals(new QName(name), impl.getName());
    }

    protected void setUp() throws Exception {
        super.setUp();
        artifactRepository = createMock(ArtifactRepository.class);
        reader = createMock(XMLStreamReader.class);
        namespaceContext = createMock(NamespaceContext.class);
        context = createMock(LoaderContext.class);
        cl = getClass().getClassLoader();
        loader = new ImplementationCompositeLoader(null, artifactRepository);
    }

    protected static Artifact artifactMatcher(final URL url,
                                              final String group,
                                              final String name,
                                              final String version) {
        reportMatcher(new IArgumentMatcher() {

            public boolean matches(Object object) {
                if (!(object instanceof Artifact)) {
                    return false;
                }

                Artifact artifact = (Artifact) object;
                boolean match = group.equals(artifact.getGroup())
                    && name.equals(artifact.getName())
                    && version.equals(artifact.getVersion())
                    && "jar".equals(artifact.getType());
                if (match) {
                    artifact.setUrl(url);
                }
                return match;
            }

            public void appendTo(StringBuffer stringBuffer) {
                stringBuffer.append(group).append(':').append(name).append(':').append(version);
            }
        });
        return null;
    }
}
