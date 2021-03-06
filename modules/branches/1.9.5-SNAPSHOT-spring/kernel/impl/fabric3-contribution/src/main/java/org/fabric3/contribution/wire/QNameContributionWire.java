/*
* Fabric3
* Copyright (c) 2009-2012 Metaform Systems
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
package org.fabric3.contribution.wire;

import java.net.URI;

import org.fabric3.spi.contribution.ContributionWire;
import org.fabric3.spi.contribution.Symbol;
import org.fabric3.spi.contribution.manifest.QNameExport;
import org.fabric3.spi.contribution.manifest.QNameImport;
import org.fabric3.spi.contribution.manifest.QNameSymbol;

/**
 * Wires two contributions using the SCA import/export mechanism, making QName-based artifacts exported from one contribution visible to the other
 * importing contribution.
 *
 * @version $Rev$ $Date$
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
