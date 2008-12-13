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
package org.fabric3.spi.contribution.manifest;

import javax.xml.namespace.QName;

import org.fabric3.spi.Namespaces;
import org.fabric3.spi.contribution.Export;
import org.fabric3.spi.contribution.Import;

/**
 * A QName-based contribution export
 *
 * @version $Rev$ $Date$
 */
public class QNameExport implements Export {
    private static final long serialVersionUID = -6813997109078522174L;
    private static final QName TYPE = new QName(Namespaces.CORE, "qNameImport");
    private QName namespace;

    public QNameExport(QName namespace) {
        this.namespace = namespace;
    }

    public QName getNamespace() {
        return namespace;
    }

    public int match(Import contributionImport) {
        if (contributionImport instanceof QNameImport
                && ((QNameImport) contributionImport).getNamespace().equals(namespace)) {
            return EXACT_MATCH;
        }
        return NO_MATCH;
    }

    public QName getType() {
        return TYPE;
    }

}
