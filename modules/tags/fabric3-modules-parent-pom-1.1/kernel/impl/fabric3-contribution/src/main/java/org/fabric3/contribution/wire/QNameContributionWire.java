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
import org.fabric3.spi.contribution.manifest.QNameExport;
import org.fabric3.spi.contribution.manifest.QNameImport;
import org.fabric3.spi.contribution.manifest.QNameSymbol;
import org.fabric3.spi.contribution.Symbol;

/**
 * Wires two contributions using the SCA import/export mechanism, making QName-based artifacts exported from one contribution visible to the other
 * importing contribution.
 *
 * @version $Revision$ $Date$
 */
public class QNameContributionWire implements ContributionWire<QNameImport, QNameExport> {
    private static final long serialVersionUID = -2760593628993100399L;
    private QNameImport imprt;
    private QNameExport export;
    private URI importUri;
    private URI exportUri;

    public QNameContributionWire(QNameImport imprt, QNameExport export, URI importUri, URI exportUri) {
        this.imprt = imprt;
        this.export = export;
        this.importUri = importUri;
        this.exportUri = exportUri;
    }

    public QNameImport getImport() {
        return imprt;
    }

    public QNameExport getExport() {
        return export;
    }

    public URI getImportContributionUri() {
        return importUri;
    }

    public URI getExportContributionUri() {
        return exportUri;
    }

    public boolean resolves(Symbol resource) {
        if (!(resource instanceof QNameSymbol)) {
            return false;
        }
        QNameSymbol symbol = (QNameSymbol) resource;
        return imprt.getNamespace().equals(symbol.getKey().getNamespaceURI());
    }
}
