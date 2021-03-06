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
package org.fabric3.contribution.wire;

import java.net.URI;

import org.fabric3.contribution.manifest.JavaExport;
import org.fabric3.contribution.manifest.JavaImport;
import org.fabric3.spi.contribution.ContributionWire;
import org.fabric3.spi.contribution.Symbol;

/**
 * Wires two contributions, using the Java import/export mechanism, making a set of classes from the exporting contribution visibile to the importing
 * contribution. The semantics of a JavaContributionWire are defined by OSGi R4 bundle imports and exports.
 *
 * @version $Revision$ $Date$
 */
public class JavaContributionWire implements ContributionWire<JavaImport, JavaExport> {
    private static final long serialVersionUID = -2724694051340291455L;
    private JavaImport imprt;
    private JavaExport export;
    private URI importUri;
    private URI exportUri;

    public JavaContributionWire(JavaImport imprt, JavaExport export, URI importUri, URI exportUri) {
        this.imprt = imprt;
        this.export = export;
        this.importUri = importUri;
        this.exportUri = exportUri;
    }

    public JavaImport getImport() {
        return imprt;
    }

    public JavaExport getExport() {
        return export;
    }

    public URI getImportContributionUri() {
        return importUri;
    }

    public URI getExportContributionUri() {
        return exportUri;
    }

    public boolean resolves(Symbol resource) {
        // return false as this wire type is used to resolve classes, which are done via a classloader
        return false;
    }

}
