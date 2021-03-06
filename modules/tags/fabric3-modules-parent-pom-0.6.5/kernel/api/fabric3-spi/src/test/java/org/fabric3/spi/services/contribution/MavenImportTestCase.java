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
package org.fabric3.spi.services.contribution;

import junit.framework.TestCase;

/**
 * @version $Rev$ $Date$
 */
public class MavenImportTestCase extends TestCase {

    public void testParse1() {
        MavenImport imprt = new MavenImport();
        imprt.setVersion("4.0.2");
        assertEquals("4",imprt.getMajorVersion());
        assertEquals("0",imprt.getMinorVersion());
        assertEquals("2",imprt.getRevision());
    }

    public void testParse2() {
        MavenImport imprt = new MavenImport();
        imprt.setVersion("4.0");
        assertEquals("4",imprt.getMajorVersion());
        assertEquals("0",imprt.getMinorVersion());
    }

    public void testParse3() {
        MavenImport imprt = new MavenImport();
        imprt.setVersion("4");
        assertEquals("4",imprt.getMajorVersion());
    }

    public void testParse4() {
        MavenImport imprt = new MavenImport();
        imprt.setVersion("4.0.2-SNAPSHOT");
        assertEquals("4",imprt.getMajorVersion());
        assertEquals("0",imprt.getMinorVersion());
        assertEquals("2",imprt.getRevision());
    }
    public void testParse5() {
        MavenImport imprt = new MavenImport();
        imprt.setVersion("4.0-SNAPSHOT");
        assertEquals("4",imprt.getMajorVersion());
        assertEquals("0",imprt.getMinorVersion());
    }

}
