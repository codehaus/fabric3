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

import java.io.ByteArrayInputStream;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

/**
 * @version $Rev$ $Date$
 */
public class JavaImportLoaderTestCase extends TestCase {
    private static final String XML_VERSION = "<import.java package=\"org.bar\" version=\"1.0.0\" required=\"true\"/>";
    private static final String XML_RANGE =
            "<import.java package=\"org.bar\" min=\"1.0.0\" minInclusive=\"false\" max=\"2.0.0\" maxInclusive=\"true\" required=\"true\"/>";
    private static final PackageVersion MIN_VERSION = new PackageVersion(1, 0, 0);
    private static final PackageVersion MAX_VERSION = new PackageVersion(2, 0, 0);

    private JavaImportLoader loader;
    private XMLStreamReader reader;

    public void testReadVersion() throws Exception {
        ByteArrayInputStream b = new ByteArrayInputStream(XML_VERSION.getBytes());
        reader = XMLInputFactory.newInstance().createXMLStreamReader(b);
        reader.nextTag();

        JavaImport jimport = loader.load(reader, null);
        PackageInfo info = jimport.getPackageInfo();
        assertEquals("org.bar", info.getName());
        assertEquals(0, info.getMinVersion().compareTo(MIN_VERSION));
        assertTrue(info.isRequired());
    }

    public void testReadVersionRange() throws Exception {
        ByteArrayInputStream b = new ByteArrayInputStream(XML_RANGE.getBytes());
        reader = XMLInputFactory.newInstance().createXMLStreamReader(b);
        reader.nextTag();

        JavaImport jimport = loader.load(reader, null);
        PackageInfo info = jimport.getPackageInfo();
        assertEquals("org.bar", info.getName());
        assertEquals(0, info.getMinVersion().compareTo(MIN_VERSION));
        assertFalse(info.isMinInclusive());
        assertEquals(0, info.getMaxVersion().compareTo(MAX_VERSION));
        assertTrue(info.isMaxInclusive());
        assertTrue(info.isRequired());
    }

    protected void setUp() throws Exception {
        super.setUp();
        loader = new JavaImportLoader();
    }


}
