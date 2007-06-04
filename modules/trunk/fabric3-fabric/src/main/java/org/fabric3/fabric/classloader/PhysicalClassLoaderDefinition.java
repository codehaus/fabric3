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

    protected PhysicalClassLoaderDefinition(URI name) {
        super(name);
    }

    /**
     * Adds a URL to the end of the classpath.
     *
     * @param url the url to add
     */
    public void addUrl(URL url) {
        urls.add(url);
    }

    /**
     * Returns the classpath as an ordered Set. Order is guaranteed for set iteration.
     *
     * @return he classpath as an ordered Set
     */
    public Set<URL> getUrls() {
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
}
