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
package org.fabric3.spi.contribution.manifest;

import java.net.URI;
import javax.xml.namespace.QName;

import org.fabric3.host.Namespaces;
import org.fabric3.spi.contribution.Import;

/**
 * Represents an <code>import.java</code> entry in a contribution manifest.
 *
 * @version $Rev$ $Date$
 */
public class JavaImport implements Import {
    private static final long serialVersionUID = -7863768515125756048L;
    private static final QName TYPE = new QName(Namespaces.CORE, "javaImport");
    private URI location;
    private PackageInfo packageInfo;

    public JavaImport(PackageInfo packageInfo) {
        this.packageInfo = packageInfo;
    }

    public URI getLocation() {
        return location;
    }

    public void setLocation(URI location) {
        this.location = location;
    }

    public QName getType() {
        return TYPE;
    }

    public PackageInfo getPackageInfo() {
        return packageInfo;
    }

    public String toString() {
        return "[" + packageInfo + "]";
    }
}
