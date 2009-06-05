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

import org.fabric3.spi.contribution.manifest.JavaExport;
import org.fabric3.spi.contribution.manifest.JavaImport;

/**
 * Creates JavaContributionWire instances.
 *
 * @version $Revision$ $Date$
 */
public class JavaContributionWireInstantiator implements ContributionWireInstantiator<JavaImport, JavaExport, JavaContributionWire> {

    public JavaContributionWire instantiate(JavaImport imprt, JavaExport export, URI importUri, URI exportUri) {
        return new JavaContributionWire(imprt, export, importUri, exportUri);
    }

}
