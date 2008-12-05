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
package org.fabric3.spi.services.contribution;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import org.fabric3.host.contribution.InstallException;
import org.fabric3.model.type.ValidationContext;

/**
 * The system registry of contribution processors
 *
 * @version $Rev$ $Date$
 */
public interface ProcessorRegistry {
    /**
     * Register a ContributionProcessor using the content type as the key
     *
     * @param processor the processor to registrer
     */
    void register(ContributionProcessor processor);

    /**
     * Unregister a ContributionProcessor for a content type
     *
     * @param contentType the content
     */
    void unregisterContributionProcessor(String contentType);

    /**
     * Register a ManifestProcessor using the content type as the key
     *
     * @param processor the processor to registrer
     */
    void register(ManifestProcessor processor);

    /**
     * Unregister a ManifestProcessor for a content type
     *
     * @param contentType the content
     */
    void unregisterManifestProcessor(String contentType);

    /**
     * Register a ResourceProcessor using the content type as the key
     *
     * @param processor the processor to registrer
     */
    void register(ResourceProcessor processor);

    /**
     * Unregister a ResourceProcessor for a content type
     *
     * @param contentType the content
     */
    void unregisterResourceProcessor(String contentType);

    /**
     * Dispatches to a {@link ContributionProcessor} to process manifest information in a contribution.
     *
     * @param contribution the contribution
     * @param context      the context to which validation errors and warnings are reported
     * @throws InstallException if there was a problem processing the manifest
     */
    void processManifest(Contribution contribution, ValidationContext context) throws InstallException;

    /**
     * Dispatches to a {@link ManifestProcessor} to process a manifest artifact contaned in a contribution.
     *
     * @param manifest    the manifest to update
     * @param contentType the artifact MIME type
     * @param inputStream the input stream for the artifact
     * @param context     the context to which validation errors and warnings are reported
     * @throws InstallException if there was a problem processing the artifact
     */
    void processManifestArtifact(ContributionManifest manifest, String contentType, InputStream inputStream, ValidationContext context)
            throws InstallException;

    /**
     * Dispatches to a {@link ContributionProcessor} to index a contribution.
     *
     * @param contribution the contribution to index
     * @param context      the context to which validation errors and warnings are reported
     * @throws InstallException if there was a problem indexing the contribution
     */
    void indexContribution(Contribution contribution, ValidationContext context) throws InstallException;

    /**
     * Dispatches to a {@link ResourceProcessor} to index a resource contained in a contribution.
     *
     * @param contribution the cntaining contribution
     * @param contentType  the content type of the resource to process
     * @param url          a dereferenceable URL for the resource
     * @param context      the context to which validation errors and warnings are reported
     * @throws InstallException if there was a problem indexing the contribution
     */
    void indexResource(Contribution contribution, String contentType, URL url, ValidationContext context) throws InstallException;

    /**
     * Loads all indexed resources in a contribution.
     *
     * @param contribution The contribution
     * @param context      the context to which validation errors and warnings are reported
     * @param loader       the classloader conribution resources must be laoded in
     * @throws InstallException if there was a problem loading resources in the contribution
     */
    void processContribution(Contribution contribution, ValidationContext context, ClassLoader loader) throws InstallException;

    /**
     * Loads a contained resource in a contribution.
     *
     * @param contributionUri the URI of the active contribution
     * @param resource        the resource to process
     * @param context         the context to which validation errors and warnings are reported
     * @param loader          the classloader contribution the resource must be loaded in
     * @throws InstallException if there was a problem loading the resoure
     */
    void processResource(URI contributionUri, Resource resource, ValidationContext context, ClassLoader loader) throws InstallException;

}
