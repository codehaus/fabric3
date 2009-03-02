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
 */
package org.fabric3.contribution.manifest;

import java.net.URI;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import static org.osoa.sca.Constants.SCA_NS;

import org.fabric3.spi.contribution.ContributionManifest;
import org.fabric3.spi.contribution.Export;
import org.fabric3.spi.contribution.Import;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.LoaderRegistry;

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
        assertTrue(manifest.isExtension());
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
        loader = new ContributionElementLoader(loaderRegistry);

        reader = EasyMock.createMock(XMLStreamReader.class);
        EasyMock.expect(reader.getAttributeCount()).andReturn(0).atLeastOnce();
        EasyMock.expect(reader.next()).andReturn(XMLStreamConstants.START_ELEMENT);
        EasyMock.expect(reader.getName()).andReturn(CONTRIBUTION);
        EasyMock.expect(reader.next()).andReturn(XMLStreamConstants.START_ELEMENT);
        EasyMock.expect(reader.getAttributeValue((String) EasyMock.isNull(),
                                                 EasyMock.eq("extension"))).andReturn("true");

        EasyMock.expect(reader.getName()).andReturn(DEPLOYABLE_ELEMENT);
        EasyMock.expect(reader.getAttributeValue((String) EasyMock.isNull(),
                                                 EasyMock.eq("composite"))).andReturn("test");
        EasyMock.expect(reader.getNamespaceURI()).andReturn(null);
        EasyMock.expect(reader.next()).andReturn(XMLStreamConstants.END_ELEMENT);
        EasyMock.expect(reader.getName()).andReturn(DEPLOYABLE_ELEMENT);

        EasyMock.expect(reader.getAttributeValue((String) EasyMock.isNull(),
                                                 EasyMock.eq("modes"))).andReturn(null);

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
