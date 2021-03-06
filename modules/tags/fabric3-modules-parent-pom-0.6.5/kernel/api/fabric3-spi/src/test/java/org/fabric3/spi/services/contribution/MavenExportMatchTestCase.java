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
public class MavenExportMatchTestCase extends TestCase {

    public void testDefaultVersionAndClassifier() {
        MavenExport export = new MavenExport();
        export.setGroupId("bar");
        export.setArtifactId("foo");
        MavenImport imprt = new MavenImport();
        imprt.setGroupId("bar");
        imprt.setArtifactId("foo");
        assertEquals(Export.EXACT_MATCH, export.match(imprt));
    }

    public void testAnyVersionImport() {
        MavenExport export = new MavenExport();
        export.setGroupId("bar");
        export.setArtifactId("foo");
        export.setVersion("2.0");
        MavenImport imprt = new MavenImport();
        imprt.setGroupId("bar");
        imprt.setArtifactId("foo");
        assertEquals(Export.EXACT_MATCH, export.match(imprt));
    }

    public void testNoMatchVersion() {
        MavenExport export = new MavenExport();
        export.setGroupId("bar");
        export.setArtifactId("foo");
        MavenImport imprt = new MavenImport();
        imprt.setGroupId("bar");
        imprt.setArtifactId("foo");
        imprt.setVersion("4.0");
        assertEquals(Export.NO_MATCH, export.match(imprt));
    }


    public void testVersion() {
        MavenExport export = new MavenExport();
        export.setGroupId("bar");
        export.setArtifactId("foo");
        export.setVersion("4.0.1");

        MavenImport imprt = new MavenImport();
        imprt.setGroupId("bar");
        imprt.setArtifactId("foo");
        imprt.setVersion("4.0");
        assertEquals(Export.EXACT_MATCH, export.match(imprt));

        imprt = new MavenImport();
        imprt.setGroupId("bar");
        imprt.setArtifactId("foo");
        imprt.setVersion("4.0.2");
        assertEquals(Export.NO_MATCH, export.match(imprt));

        imprt = new MavenImport();
        imprt.setGroupId("bar");
        imprt.setArtifactId("foo");
        imprt.setVersion("4.2.1");
        assertEquals(Export.NO_MATCH, export.match(imprt));

        imprt = new MavenImport();
        imprt.setGroupId("bar");
        imprt.setArtifactId("foo");
        imprt.setVersion("3.0");
        assertEquals(Export.EXACT_MATCH, export.match(imprt));

        imprt = new MavenImport();
        imprt.setGroupId("bar");
        imprt.setArtifactId("foo");
        imprt.setVersion("5.0");
        assertEquals(Export.NO_MATCH, export.match(imprt));

        imprt = new MavenImport();
        imprt.setGroupId("bar");
        imprt.setArtifactId("foo");
        imprt.setVersion("5.0.1");
        assertEquals(Export.NO_MATCH, export.match(imprt));

        imprt = new MavenImport();
        imprt.setGroupId("bar");
        imprt.setArtifactId("foo");
        imprt.setVersion("4.0.1-SNAPSHOT");
        assertEquals(Export.EXACT_MATCH, export.match(imprt));

    }

    public void testExportSnapshotVersion() {
        MavenExport export = new MavenExport();
        export.setGroupId("bar");
        export.setArtifactId("foo");
        export.setVersion("4.0.1-SNAPSHOT");
                         
        MavenImport imprt = new MavenImport();
        imprt.setGroupId("bar");
        imprt.setArtifactId("foo");
        imprt.setVersion("4.0.1");
        assertEquals(Export.NO_MATCH, export.match(imprt));
    }

}