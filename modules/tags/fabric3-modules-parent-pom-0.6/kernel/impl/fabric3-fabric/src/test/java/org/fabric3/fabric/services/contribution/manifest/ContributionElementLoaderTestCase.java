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
package org.fabric3.fabric.services.contribution.manifest;

import java.net.URI;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import static org.osoa.sca.Constants.SCA_NS;

import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.xml.LoaderHelper;
import org.fabric3.introspection.xml.LoaderRegistry;
import org.fabric3.loader.impl.DefaultLoaderHelper;
import org.fabric3.spi.services.contribution.ContributionManifest;
import org.fabric3.spi.services.contribution.Export;
import org.fabric3.spi.services.contribution.Import;

/**
 * @version $Rev$ $Date$
 */
public class ContributionElementLoaderTestCase extends TestCase {
    private static final QName CONTRIBUTION = new QName(SCA_NS, "contribution");
    private static final QName DEPLOYABLE_ELEMENT = new QName(SCA_NS, "deployable");
    private static final QName IMPORT_ELEMENT = new QName(SCA_NS, "import");
    private static final QName EXPORT_ELEMENT = new QName(SCA_NS, "export");
    private static final QName DEPLOYABLE = new QName("test");

    private ContributionElementLoader loader;
    private XMLStreamReader reader;
    private IMocksControl control;

    public void testDispatch() throws Exception {
        ContributionManifest manifest = loader.load(reader, null);
        control.verify();
        assertEquals(1, manifest.getDeployables().size());
        assertEquals(DEPLOYABLE, manifest.getDeployables().get(0).getName());
        assertEquals(1, manifest.getExports().size());
        assertEquals(1, manifest.getImports().size());
    }

    @SuppressWarnings({"serial"})
    protected void setUp() throws Exception {
        super.setUp();
        control = EasyMock.createStrictControl();
        LoaderRegistry loaderRegistry = EasyMock.createMock(LoaderRegistry.class);
        LoaderHelper helper = new DefaultLoaderHelper();
        loader = new ContributionElementLoader(loaderRegistry, helper);

        reader = EasyMock.createMock(XMLStreamReader.class);
        EasyMock.expect(reader.getAttributeCount()).andReturn(0);
        EasyMock.expect(reader.next()).andReturn(XMLStreamConstants.START_ELEMENT);
        EasyMock.expect(reader.getName()).andReturn(CONTRIBUTION);
        EasyMock.expect(reader.next()).andReturn(XMLStreamConstants.START_ELEMENT);
        EasyMock.expect(reader.getName()).andReturn(DEPLOYABLE_ELEMENT);
        EasyMock.expect(reader.getAttributeValue((String) EasyMock.isNull(),
                                                 EasyMock.eq("composite"))).andReturn("test");
        EasyMock.expect(reader.getNamespaceURI()).andReturn(null);
        EasyMock.expect(reader.next()).andReturn(XMLStreamConstants.END_ELEMENT);
        EasyMock.expect(reader.getName()).andReturn(DEPLOYABLE_ELEMENT);

        EasyMock.expect(reader.next()).andReturn(XMLStreamConstants.START_ELEMENT);
        EasyMock.expect(reader.getName()).andReturn(IMPORT_ELEMENT);
        Import contribImport = new Import() {
            public URI getLocation() {
                 return null;
            }

            public void setLocation(URI location) {

            }

            public QName getType() {
                return null;
            }
        };
        EasyMock.expect(loaderRegistry.load(
                EasyMock.isA(XMLStreamReader.class),
                EasyMock.eq(Object.class), (IntrospectionContext) EasyMock.isNull())).andReturn(contribImport);
        EasyMock.expect(reader.next()).andReturn(XMLStreamConstants.END_ELEMENT);
        EasyMock.expect(reader.getName()).andReturn(IMPORT_ELEMENT);

        EasyMock.expect(reader.next()).andReturn(XMLStreamConstants.START_ELEMENT);
        EasyMock.expect(reader.getName()).andReturn(EXPORT_ELEMENT);
        Export contribExport = new Export() {
            public int match(Import contributionImport) {
                return NO_MATCH;
            }

            public QName getType() {
                return null;
            }
        };
        EasyMock.expect(loaderRegistry.load(
                EasyMock.isA(XMLStreamReader.class),
                EasyMock.eq(Object.class), (IntrospectionContext) EasyMock.isNull())).andReturn(contribExport);
        EasyMock.expect(reader.next()).andReturn(XMLStreamConstants.END_ELEMENT);
        EasyMock.expect(reader.getName()).andReturn(EXPORT_ELEMENT);

        EasyMock.expect(reader.next()).andReturn(XMLStreamConstants.END_ELEMENT);
        EasyMock.expect(reader.getName()).andReturn(CONTRIBUTION);
        EasyMock.replay(loaderRegistry);
        EasyMock.replay(reader);
        control.replay();

    }


}
