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
package org.fabric3.spi.contribution;

import java.io.InputStream;

import org.fabric3.host.contribution.InstallException;
import org.fabric3.model.type.ValidationContext;
import org.fabric3.spi.contribution.ContributionManifest;

/**
 * Processes an artifact containing manifest information in a contribution archive.
 *
 * @version $Rev: 6194 $ $Date: 2008-12-05 07:52:09 -0800 (Fri, 05 Dec 2008) $
 */
public interface ManifestProcessor {

    /**
     * Returns the content type the processor handles
     *
     * @return the content type the processor handles
     */
    String getContentType();

    /**
     * Processes the input stream for the artifact and update the manifest.
     *
     * @param manifest the manifest to update
     * @param stream   the stream for the artifact
     * @param context  the context to which validation errors and warnings are reported
     * @throws InstallException if an error occurs processing the stream
     */
    void process(ContributionManifest manifest, InputStream stream, ValidationContext context) throws InstallException;

}

