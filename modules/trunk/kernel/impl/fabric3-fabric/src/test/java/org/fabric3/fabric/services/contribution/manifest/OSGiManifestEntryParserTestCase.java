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
package org.fabric3.fabric.services.contribution.manifest;

import junit.framework.TestCase;

/**
 * @version $Revision$ $Date$
 */
public class OSGiManifestEntryParserTestCase extends TestCase {
    private static final String HEADER_1 = "org.fabric3.foo;resolution:=required,org.fabric3.bar;resolution:=optional,org.fabric3.baz;version\n" +
            " =\"[1.0.0, 2.0.0)\"\n";

    private static final String HEADER_2 = "org.fabric3.baz;version=1.0.0";

    private static final String HEADER_3 = "org.fabric3.baz;version=\"[1.0.0, 2.0.0]\";resolution:=required";

    public void testHeader1() {
        OSGiManifestEntryParser parser = new OSGiManifestEntryParser(HEADER_1);
        OSGiManifestEntryParser.EventType type = parser.next();
        assertEquals(OSGiManifestEntryParser.EventType.PATH, type);
        assertEquals("org.fabric3.foo", parser.getText());
        type = parser.next();
        assertEquals(OSGiManifestEntryParser.EventType.PARAMETER, type);
        assertEquals("resolution:=required", parser.getText());
        type = parser.next();
        assertEquals(OSGiManifestEntryParser.EventType.PATH, type);
        assertEquals("org.fabric3.bar", parser.getText());
        type = parser.next();
        assertEquals(OSGiManifestEntryParser.EventType.PARAMETER, type);
        assertEquals("resolution:=optional", parser.getText());
        type = parser.next();
        assertEquals(OSGiManifestEntryParser.EventType.PATH, type);
        assertEquals("org.fabric3.baz", parser.getText());
        type = parser.next();
        assertEquals(OSGiManifestEntryParser.EventType.PARAMETER, type);
        assertEquals("version=\"[1.0.0,2.0.0)\"", parser.getText());
        type = parser.next();
        assertEquals(OSGiManifestEntryParser.EventType.END, type);
    }

    public void testHeader2() {
        OSGiManifestEntryParser parser = new OSGiManifestEntryParser(HEADER_2);
        OSGiManifestEntryParser.EventType type = parser.next();
        assertEquals(OSGiManifestEntryParser.EventType.PATH, type);
        assertEquals("org.fabric3.baz", parser.getText());
        type = parser.next();
        assertEquals(OSGiManifestEntryParser.EventType.PARAMETER, type);
        assertEquals("version=1.0.0", parser.getText());
        type = parser.next();
        assertEquals(OSGiManifestEntryParser.EventType.END, type);
    }

    public void testHeader3() {
        OSGiManifestEntryParser parser = new OSGiManifestEntryParser(HEADER_3);
        OSGiManifestEntryParser.EventType type = parser.next();
        assertEquals(OSGiManifestEntryParser.EventType.PATH, type);
        assertEquals("org.fabric3.baz", parser.getText());
        type = parser.next();
        assertEquals(OSGiManifestEntryParser.EventType.PARAMETER, type);
        assertEquals("version=\"[1.0.0,2.0.0]\"", parser.getText());
        type = parser.next();
        assertEquals(OSGiManifestEntryParser.EventType.PARAMETER, type);
        assertEquals("resolution:=required", parser.getText());
        type = parser.next();
        assertEquals(OSGiManifestEntryParser.EventType.END, type);
    }

}
