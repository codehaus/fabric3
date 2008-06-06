/*
 * See the NOTICE file distributed with this work for information
 * regarding copyright ownership.  This file is licensed
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

import junit.framework.TestCase;

import org.fabric3.spi.services.contribution.Export;

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