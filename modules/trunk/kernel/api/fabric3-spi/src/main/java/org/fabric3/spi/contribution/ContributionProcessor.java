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

import java.util.List;

import org.fabric3.host.contribution.InstallException;
import org.fabric3.model.type.ValidationContext;

/**
 * Interface for services that process contributions. Contribution processing occurs in several phases. Contribution metadata is first processed,
 * after which contained resources are indexed. Indexed {@link Resource}s contain 0..n {@link ResourceElement}s, which are addressable parts.
 * ResourceElements contain a key for a symbol space and a value. When a resource is indexed, only ResourceElement keys are available; their values
 * have not yet been loaded.
 * <p/>
 * The final processing phase is when the contribution is loaded. At this point, all contribution artifacts, including those in depedent
 * contributions, are made available through the provided classloader. Indexed Resources are iterated and all ResourceElement values are loaded via
 * the loader framework. As ResourceElements may refer to other ResourceElements, loading may ocurr recursively.
 *
 * @version $Rev$ $Date$
 */
public interface ContributionProcessor {
    /**
     * Returns the content type this implementation handles.
     *
     * @return the content type this implementation handles
     */
    public abstract List<String> getContentTypes();

    /**
     * Processses manifest information for the contribution, including imports and exports.
     *
     * @param contribution the contribution that will be used to hold the results from the processing
     * @param context      the context to which validation errors and warnings are reported
     * @throws InstallException if there was a problem with the contribution
     */
    void processManifest(Contribution contribution, ValidationContext context) throws InstallException;

    /**
     * Indexes all contribution resources
     *
     * @param contribution the contribution to index
     * @param context      the context to which validation errors and warnings are reported
     * @throws InstallException if there was a problem indexing
     */
    void index(Contribution contribution, ValidationContext context) throws InstallException;

    /**
     * Loads all resources in the contribution.
     *
     * @param contribution the contribution
     * @param context      the context to which validation errors and warnings are reported
     * @param loader       the classloader contribution resources must be loaded in
     * @throws InstallException if there was a problem loading the contribution resoruces
     */
    public void process(Contribution contribution, ValidationContext context, ClassLoader loader) throws InstallException;

}
