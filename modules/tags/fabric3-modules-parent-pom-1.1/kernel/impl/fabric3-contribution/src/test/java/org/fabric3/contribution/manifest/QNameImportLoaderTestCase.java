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
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.spi.contribution.manifest.QNameImport;

/**
 * @version $Rev$ $Date$
 */
public class QNameImportLoaderTestCase extends TestCase {
    private static final String QNAME = "namespace";
    private static final URI LOCATION = URI.create("location");
    private QNameImportLoader loader = new QNameImportLoader();
    private XMLStreamReader reader;

    public void testRead() throws Exception {
        QNameImport qimport = loader.load(reader, null);
        assertEquals(QNAME, qimport.getNamespace());
        assertEquals(LOCATION, qimport.getLocation());
    }


    protected void setUp() throws Exception {
        super.setUp();
        reader = EasyMock.createMock(XMLStreamReader.class);
        EasyMock.expect(reader.getAttributeCount()).andReturn(0);
        EasyMock.expect(reader.getAttributeValue(null, "namespace")).andReturn("namespace");
        EasyMock.expect(reader.getAttributeValue(null, "location")).andReturn("location");
        EasyMock.replay(reader);
    }
}
