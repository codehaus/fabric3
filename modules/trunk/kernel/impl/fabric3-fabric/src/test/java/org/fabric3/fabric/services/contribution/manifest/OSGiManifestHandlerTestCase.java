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

import java.io.ByteArrayInputStream;
import java.util.jar.Manifest;

import junit.framework.TestCase;

import org.fabric3.model.type.DefaultValidationContext;
import org.fabric3.spi.services.contribution.ContributionManifest;

/**
 * @version $Revision$ $Date$
 */
public class OSGiManifestHandlerTestCase extends TestCase {
    private static final String MANIFEST = "Manifest-Version: 1.0\n" +
            "Created-By: test\n" +
            "Import-Package: org.fabric3.foo;resolution:=required,org.fabric3.bar;resolution:=optional,org.fabric3.baz;version" +
            " =\"[1.0.0, 2.0.0)\"\n" +
            "Export-Package: org.fabric3.export1;version=\"1.1.1.1\",org.fabric3.export2;version=\"2.2.2.2\";uses:=\"foo.com, bar.com\"\n";
    private OSGiManifestHandler handler = new OSGiManifestHandler();

    public void testHeaderParse() throws Exception {
        Manifest manifest = new Manifest(new ByteArrayInputStream(MANIFEST.getBytes()));
        ContributionManifest contributionManifest = new ContributionManifest();
        DefaultValidationContext context = new DefaultValidationContext();
        handler.processManifest(contributionManifest, manifest, context);

        assertFalse(context.hasErrors());

        assertEquals(3, contributionManifest.getImports().size());
        JavaImport first = (JavaImport) contributionManifest.getImports().get(0);
        assertEquals("org.fabric3.foo", first.getPackageInfo().getName());
        assertTrue(first.getPackageInfo().isRequired());

        JavaImport second = (JavaImport) contributionManifest.getImports().get(1);
        assertEquals("org.fabric3.bar", second.getPackageInfo().getName());
        assertFalse(second.getPackageInfo().isRequired());

        JavaImport third = (JavaImport) contributionManifest.getImports().get(2);
        assertEquals("org.fabric3.baz", third.getPackageInfo().getName());
        assertEquals(new PackageVersion("1.0.0"), third.getPackageInfo().getMinVersion());
        assertTrue(third.getPackageInfo().isMinInclusive());
        assertEquals(new PackageVersion("2.0.0"), third.getPackageInfo().getMaxVersion());
        assertFalse(third.getPackageInfo().isMaxInclusive());

        assertEquals(2, contributionManifest.getExports().size());
        JavaExport firstExport = (JavaExport) contributionManifest.getExports().get(0);
        assertEquals("org.fabric3.export1", firstExport.getPackageInfo().getName());
        assertEquals(new PackageVersion("1.1.1.1"), firstExport.getPackageInfo().getMinVersion());

        JavaExport secondExport = (JavaExport) contributionManifest.getExports().get(1);
        assertEquals("org.fabric3.export2", secondExport.getPackageInfo().getName());
        assertEquals(new PackageVersion("2.2.2.2"), secondExport.getPackageInfo().getMinVersion());

    }

}
