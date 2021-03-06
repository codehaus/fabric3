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
package org.fabric3.spi.model.physical;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.io.Serializable;

/**
 * A definition used to provision classloaders on a runtime.
 *
 * @version $Rev$ $Date$
 */
public class PhysicalClassLoaderDefinition implements Serializable {
    private static final long serialVersionUID = 1869864181383360066L;

    private URI uri;
    private List<URI> parentClassLoaders = new ArrayList<URI>();
    private Set<URI> contributionUris = new LinkedHashSet<URI>();

    public PhysicalClassLoaderDefinition(URI uri) {
        this.uri = uri;
    }

    /**
     * Returns the classloader uri.
     *
     * @return the classloader uri
     */
    public URI getUri() {
        return uri;
    }

    /**
     * Associates the classloader with a contribution. When a classloader is created, a copy of the contribution will be avilable on the classpath.
     *
     * @param uri the URI to add
     */
    public void addContributionUri(URI uri) {
        contributionUris.add(uri);
    }

    /**
     * Returns the URIs of contributions associated with this classloader as an ordered Set. Order is guaranteed for set iteration.
     *
     * @return the URIs as an ordered Set
     */
    public Set<URI> getContributionUris() {
        return contributionUris;
    }

    /**
     * Returns the list of parent classloader URIs.
     *
     * @return the list of parent classloader URIs
     */
    public List<URI> getParentClassLoaders() {
        return parentClassLoaders;
    }

    /**
     * Adds a parent classloader URI.
     *
     * @param uri the classloader URI
     */
    public void addParentClassLoader(URI uri) {
        parentClassLoaders.add(uri);
    }

    @Override
    public boolean equals(Object obj) {

        if (obj == null || obj.getClass() != PhysicalClassLoaderDefinition.class) {
            return false;
        }

        PhysicalClassLoaderDefinition other = (PhysicalClassLoaderDefinition) obj;

        return parentClassLoaders.equals(other.parentClassLoaders)
                && contributionUris.equals(other.contributionUris)
                && uri.equals(other.uri);
    }

    public int hashCode() {
        int result;
        result = (uri != null ? uri.hashCode() : 0);
        result = 31 * result + (parentClassLoaders != null ? parentClassLoaders.hashCode() : 0);
        result = 31 * result + (contributionUris != null ? contributionUris.hashCode() : 0);
        return result;
    }
}
