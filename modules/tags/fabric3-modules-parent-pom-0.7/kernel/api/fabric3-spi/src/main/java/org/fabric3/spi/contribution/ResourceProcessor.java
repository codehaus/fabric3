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

import java.net.URI;
import java.net.URL;

import org.fabric3.host.contribution.InstallException;
import org.fabric3.model.type.ValidationContext;

/**
 * Implmentations process a contribution resource for a MIME type.
 *
 * @version $Rev$ $Date$
 */
public interface ResourceProcessor {

    /**
     * Returns the content type the processor handles
     *
     * @return the content type the processor handles
     */
    String getContentType();

    /**
     * Indexes the resource
     *
     * @param contribution the containing contribution
     * @param url          a dereferenceable url to the resource
     * @param context      the context to which validation errors and warnings are reported
     * @throws InstallException if an error occurs during indexing
     */
    void index(Contribution contribution, URL url, ValidationContext context) throws InstallException;

    /**
     * Loads the the Resource
     *
     * @param contributionUri the URI of the active contribution
     * @param resource        the resource to process
     * @param context         the context to which validation errors and warnings are reported
     * @param loader          the classloader contribution the resource must be loaded in @throws ContributionException if an error occurs during
     *                        introspection
     * @throws InstallException if an error processing the contribution occurs
     */
    void process(URI contributionUri, Resource resource, ValidationContext context, ClassLoader loader) throws InstallException;

}
