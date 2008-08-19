/*
 * See the NOTICE file distributed with this work for information
 * regarding copyright ownership.  This file is licensed
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.fabric3.spi.model.physical;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * A definition used to provision classloaders on a runtime.
 *
 * @version $Rev$ $Date$
 */
public class PhysicalClassLoaderDefinition {
    private URI uri;
    private List<URI> parentClassLoaders = new ArrayList<URI>();
    private Set<URI> contributionUris = new LinkedHashSet<URI>();
    private Set<URI> extensionUris = new LinkedHashSet<URI>();

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
     * Associates the classloader with an extension. When a classloader is created, the extension classes will be visible to the classloader.
     *
     * @param uri the URI to add
     */
    public void addExtensionUri(URI uri) {
        extensionUris.add(uri);
    }

    /**
     * Returns the URIs of extensions associated with this classloader as an ordered Set. Order is guaranteed for set iteration.
     *
     * @return the URIs as an ordered Set
     */
    public Set<URI> getExtensionUris() {
        return extensionUris;
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
                && extensionUris.equals(other.extensionUris)
                && uri.equals(other.uri);
    }

    public int hashCode() {
        int result;
        result = (uri != null ? uri.hashCode() : 0);
        result = 31 * result + (parentClassLoaders != null ? parentClassLoaders.hashCode() : 0);
        result = 31 * result + (contributionUris != null ? contributionUris.hashCode() : 0);
        result = 31 * result + (extensionUris != null ? extensionUris.hashCode() : 0);
        return result;
    }
}
