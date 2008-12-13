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

import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.spi.services.contribution.manifest.MavenExport;

/**
 * @version $Rev$ $Date$
 */
public class MavenExportLoaderTestCase extends TestCase {
    private MavenExportLoader loader = new MavenExportLoader();
    private XMLStreamReader reader;

    public void testRead() throws Exception {
        MavenExport export = loader.load(reader, null);
        assertEquals("foo", export.getGroupId());
        assertEquals("bar", export.getArtifactId());
        assertEquals("1.0-SNAPSHOT", export.getVersion());
        assertEquals("zip", export.getClassifier());
    }


    protected void setUp() throws Exception {
        super.setUp();
        reader = EasyMock.createMock(XMLStreamReader.class);
        EasyMock.expect(reader.getAttributeCount()).andReturn(0);
        EasyMock.expect(reader.getAttributeValue(null, "groupId")).andReturn("foo");
        EasyMock.expect(reader.getAttributeValue(null, "artifactId")).andReturn("bar");
        EasyMock.expect(reader.getAttributeValue(null, "version")).andReturn("1.0-SNAPSHOT");
        EasyMock.expect(reader.getAttributeValue(null, "classifier")).andReturn("zip");
        EasyMock.replay(reader);
    }
}
