/*
 * Fabric3
 * Copyright � 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the �License�), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an �as is� basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.contribution.manifest;

import java.io.ByteArrayInputStream;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

import org.fabric3.spi.contribution.manifest.JavaExport;
import org.fabric3.spi.contribution.manifest.PackageInfo;

/**
 * @version $Rev$ $Date$
 */
public class JavaExportLoaderTestCase extends TestCase {
    private static final String XML = "<export.java package=\"org.bar\" version=\"1.0.1\"/>";

    private JavaExportLoader loader;
    private XMLStreamReader reader;

    public void testRead() throws Exception {
        JavaExport export = loader.load(reader, null);
        PackageInfo info = export.getPackageInfo();
        assertEquals("org.bar", info.getName());
        assertEquals(1, info.getMinVersion().getMajor());
        assertEquals(0, info.getMinVersion().getMinor());
        assertEquals(1, info.getMinVersion().getMicro());
    }


    protected void setUp() throws Exception {
        super.setUp();
        loader = new JavaExportLoader();
        ByteArrayInputStream b = new ByteArrayInputStream(XML.getBytes());
        reader = XMLInputFactory.newInstance().createXMLStreamReader(b);
        reader.nextTag();
    }
}
