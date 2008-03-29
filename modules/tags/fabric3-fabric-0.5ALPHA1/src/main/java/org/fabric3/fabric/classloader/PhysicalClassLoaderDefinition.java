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
package org.fabric3.fabric.classloader;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.fabric3.spi.model.physical.PhysicalResourceContainerDefinition;

/**
 * A resource container definition used to provision composite classloaders on a service node from a physical change
 * set.
 *
 * @version $Rev$ $Date$
 */
public class PhysicalClassLoaderDefinition extends PhysicalResourceContainerDefinition {
    
    private List<URI> parentClassLoaders = new ArrayList<URI>();
    private Set<URL> urls = new LinkedHashSet<URL>();
    private boolean update;

    protected PhysicalClassLoaderDefinition(URI name) {
        super(name);
    }

    /**
     * Adds a remotely dereferenceable resource URL to the container definition. When a classloader is created, a copy
     * of the resource will be avilable on the classpath.
     *
     * @param url the URL to add
     */
    public void addResourceUrl(URL url) {
        urls.add(url);
    }

    /**
     * Returns the dereferenceable resource URLs for the container definition as an ordered Set. Order is guaranteed for
     * set iteration.
     *
     * @return the resource URLs as an ordered Set
     */
    public Set<URL> getResourceUrls() {
        return Collections.unmodifiableSet(urls);
    }

    /**
     * Returns the list of parent classloader URIs.
     *
     * @return the list of parent classloader URIs
     */
    public List<URI> getParentClassLoaders() {
        return Collections.unmodifiableList(parentClassLoaders);
    }

    /**
     * Adds a parent classloader URI.
     *
     * @param uri the classloader URI
     */
    public void addParentClassLoader(URI uri) {
        parentClassLoaders.add(uri);
    }

    public boolean isUpdate() {
        return update;
    }

    public void setUpdate(boolean update) {
        this.update = update;
    }

    @Override
    public boolean equals(Object obj) {
        
        if (obj == null || obj.getClass() != PhysicalClassLoaderDefinition.class) {
            return false;
        }
        
        PhysicalClassLoaderDefinition other = (PhysicalClassLoaderDefinition) obj;
        
        return parentClassLoaders.equals(other.parentClassLoaders) && 
               urls.equals(other.urls) && 
               update == other.update && 
               getUri().equals(other.getUri());
    }

    @Override
    public int hashCode() {
        
        int hash = 7;
        hash = 31 * hash + getUri().hashCode();
        hash = 31 * hash + parentClassLoaders.hashCode();
        hash = 31 * hash + urls.hashCode();
        hash = 31 * hash + (update ? 0 : 1);
        
        return hash;
    }
}
