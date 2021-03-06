/*
* Fabric3
* Copyright (c) 2009 Metaform Systems
*
* Fabric3 is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as
* published by the Free Software Foundation, either version 3 of
* the License, or (at your option) any later version, with the
* following exception:
*
* Linking this software statically or dynamically with other
* modules is making a combined work based on this software.
* Thus, the terms and conditions of the GNU General Public
* License cover the whole combination.
*
* As a special exception, the copyright holders of this software
* give you permission to link this software with independent
* modules to produce an executable, regardless of the license
* terms of these independent modules, and to copy and distribute
* the resulting executable under terms of your choice, provided
* that you also meet, for each linked independent module, the
* terms and conditions of the license of that module. An
* independent module is a module which is not derived from or
* based on this software. If you modify this software, you may
* extend this exception to your version of the software, but
* you are not obligated to do so. If you do not wish to do so,
* delete this exception statement from your version.
*
* Fabric3 is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty
* of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU General Public License for more details.
*
* You should have received a copy of the
* GNU General Public License along with Fabric3.
* If not, see <http://www.gnu.org/licenses/>.
*/
package org.fabric3.contribution.manifest;

import java.io.ByteArrayInputStream;
import java.util.jar.Manifest;

import junit.framework.TestCase;

import org.fabric3.spi.contribution.ContributionManifest;
import org.fabric3.spi.contribution.manifest.JavaExport;
import org.fabric3.spi.contribution.manifest.JavaImport;
import org.fabric3.spi.contribution.manifest.PackageVersion;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;

/**
 * @version $Rev$ $Date$
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
        IntrospectionContext context = new DefaultIntrospectionContext();
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
