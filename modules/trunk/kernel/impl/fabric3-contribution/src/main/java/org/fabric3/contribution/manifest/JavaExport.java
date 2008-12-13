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

import javax.xml.namespace.QName;

import org.fabric3.spi.Namespaces;
import org.fabric3.spi.services.contribution.Export;
import org.fabric3.spi.services.contribution.Import;

/**
 * Represents an <code>export.java</code> entry in a contribution manifest.
 *
 * @version $Rev$ $Date$
 */
public class JavaExport implements Export {
    private static final long serialVersionUID = -1362112844218693711L;
    private static final QName TYPE = new QName(Namespaces.CORE, "javaImport");
    private PackageInfo packageInfo;

    public JavaExport(PackageInfo packageInfo) {
        this.packageInfo = packageInfo;
    }

    public QName getType() {
        return TYPE;
    }

    public PackageInfo getPackageInfo() {
        return packageInfo;
    }

    public int match(Import imprt) {
        if (imprt instanceof JavaImport) {
            JavaImport javaImport = (JavaImport) imprt;
            if (javaImport.getPackageInfo().matches(packageInfo)) {
                return EXACT_MATCH;
            }
        }
        return NO_MATCH;
    }

    public String toString() {
        return "Exported package [" + packageInfo + "]";
    }

}

