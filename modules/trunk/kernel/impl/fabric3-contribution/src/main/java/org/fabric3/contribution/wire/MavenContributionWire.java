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
package org.fabric3.contribution.wire;

import java.net.URI;

import org.fabric3.spi.contribution.ContributionWire;
import org.fabric3.spi.contribution.manifest.MavenExport;
import org.fabric3.spi.contribution.manifest.MavenImport;
import org.fabric3.spi.contribution.manifest.QNameSymbol;
import org.fabric3.spi.contribution.Symbol;

/**
 * Wires two contributions using Maven artifact identifiers, making all artifacts from one contribution visible to the other importing contribution.
 *
 * @version $Revision$ $Date$
 */
public class MavenContributionWire implements ContributionWire<MavenImport, MavenExport> {
    private static final long serialVersionUID = -2724694051340291455L;
    private MavenImport imprt;
    private MavenExport export;
    private URI importUri;
    private URI exportUri;

    public MavenContributionWire(MavenImport imprt, MavenExport export, URI importUri, URI exportUri) {
        this.imprt = imprt;
        this.export = export;
        this.importUri = importUri;
        this.exportUri = exportUri;
    }

    public MavenImport getImport() {
        return imprt;
    }

    public MavenExport getExport() {
        return export;
    }

    public URI getImportContributionUri() {
        return importUri;
    }

    public URI getExportContributionUri() {
        return exportUri;
    }

    public boolean resolves(Symbol resource) {
        // XCV
        // this is for backward compatibility
        return resource instanceof QNameSymbol;
    }

}