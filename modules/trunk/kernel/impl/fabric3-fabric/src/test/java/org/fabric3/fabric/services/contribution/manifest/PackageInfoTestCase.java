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
public class PackageInfoTestCase extends TestCase {

    public void testMatchNameWildCard() throws Exception {
        PackageInfo imprt = new PackageInfo("foo.bar.Baz");
        PackageInfo export = new PackageInfo("foo.bar.*");
        assertTrue(imprt.matches(export));
    }

    public void testMatchSubPackage() throws Exception {
        PackageInfo imprt = new PackageInfo("foo.bar.sub");
        PackageInfo export = new PackageInfo("foo.bar");
        assertFalse(imprt.matches(export));
    }

    public void testMatchNameSecondLevelWildCard() throws Exception {
        PackageInfo imprt = new PackageInfo("foo.bar.Baz");
        PackageInfo export = new PackageInfo("foo.*");
        assertTrue(imprt.matches(export));
    }

    public void testNoMatchName() throws Exception {
        PackageInfo imprt = new PackageInfo("foo.bar.Baz");
        PackageInfo export = new PackageInfo("foodbardBaz");
        assertFalse(imprt.matches(export));
    }

    public void testMatchSubpackageName() throws Exception {
        PackageInfo imprt = new PackageInfo("foo.bar.Baz");
        PackageInfo export = new PackageInfo("foo.bar.baz.Baz");
        assertFalse(imprt.matches(export));
    }

    public void testMatchSpecificVersion() throws Exception {
        PackageVersion version = new PackageVersion(1, 2, 3, "alpha");
        PackageInfo imprt = new PackageInfo("foo.bar.Baz", version, true, true);
        PackageInfo export = new PackageInfo("foo.bar.*", version, true, true);
        assertTrue(imprt.matches(export));
    }

    public void testMatchWildCardImport() throws Exception {
        PackageInfo imprt = new PackageInfo("foo.*");
        PackageInfo export = new PackageInfo("foo.*");
        assertTrue(imprt.matches(export));
    }

    public void testNoMatchWildCardImport() throws Exception {
        PackageInfo imprt = new PackageInfo("foo.*");
        PackageInfo export = new PackageInfo("foo.bar.Baz");
        assertFalse(imprt.matches(export));
    }

    public void testMatchRangeVersion() throws Exception {
        PackageVersion min = new PackageVersion(1, 0, 0);
        PackageVersion max = new PackageVersion(2, 0, 0);
        PackageVersion exportedVersion = new PackageVersion(1, 5, 0);

        PackageInfo imprt = new PackageInfo("foo.bar.Baz", min, true, max, true, true);
        PackageInfo export = new PackageInfo("foo.bar.*", exportedVersion, true, true);
        assertTrue(imprt.matches(export));
    }

    public void testMatchRangeVersionMinExclusive() throws Exception {
        PackageVersion min = new PackageVersion(1, 0, 0);
        PackageVersion exportedVersion = new PackageVersion(1, 0, 0);

        PackageInfo imprt = new PackageInfo("foo.bar", min, false, true);
        PackageInfo export = new PackageInfo("foo.bar", exportedVersion, true, true);
        assertFalse(imprt.matches(export));
    }

    public void testMatchRangeVersionMinInclusive() throws Exception {
        PackageVersion min = new PackageVersion(1, 0, 0);
        PackageVersion exportedVersion = new PackageVersion(1, 0, 0);

        PackageInfo imprt = new PackageInfo("foo.bar", min, true, true);
        PackageInfo export = new PackageInfo("foo.bar", exportedVersion, true, true);
        assertTrue(imprt.matches(export));
    }

    public void testMatchRangeVersionMaxExclusive() throws Exception {
        PackageVersion min = new PackageVersion(1, 0, 0);
        PackageVersion max = new PackageVersion(2, 0, 0);
        PackageVersion exportedVersion = new PackageVersion(2, 0, 0);

        PackageInfo imprt = new PackageInfo("foo.bar", min, true, max, false, true);
        PackageInfo export = new PackageInfo("foo.bar", exportedVersion, true, true);
        assertFalse(imprt.matches(export));
    }

    public void testMatchRangeVersionMaxInclusive() throws Exception {
        PackageVersion min = new PackageVersion(1, 0, 0);
        PackageVersion max = new PackageVersion(2, 0, 0);
        PackageVersion exportedVersion = new PackageVersion(2, 0, 0);

        PackageInfo imprt = new PackageInfo("foo.bar", min, true, max, true, true);
        PackageInfo export = new PackageInfo("foo.bar", exportedVersion, true, true);
        assertTrue(imprt.matches(export));
    }


    public void testOutOfMinRange() throws Exception {
        PackageVersion min = new PackageVersion(1, 6, 0);
        PackageVersion max = new PackageVersion(2, 0, 0);
        PackageVersion exportedVersion = new PackageVersion(1, 5, 0);

        PackageInfo imprt = new PackageInfo("foo.bar.Baz", min, true, max, true, true);
        PackageInfo export = new PackageInfo("foo.bar.*", exportedVersion, true, true);
        assertFalse(imprt.matches(export));
    }

    public void testOutOfMaxRange() throws Exception {
        PackageVersion min = new PackageVersion(1, 0, 0);
        PackageVersion max = new PackageVersion(2, 0, 0);
        PackageVersion exportedVersion = new PackageVersion(2, 5, 0);

        PackageInfo imprt = new PackageInfo("foo.bar.Baz", min, true, max, true, true);
        PackageInfo export = new PackageInfo("foo.bar.*", exportedVersion, true, true);
        assertFalse(imprt.matches(export));
    }

}
