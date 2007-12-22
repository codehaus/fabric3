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
package org.fabric3.spi.model.topology;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.fabric3.scdl.ResourceDescription;

/**
 * @version $Rev$ $Date$
 */
public class ClassLoaderResourceDescription extends ResourceDescription<URI> {
    private List<URI> parents = new ArrayList<URI>();
    private List<URL> classPathUrls = new ArrayList<URL>();

    public ClassLoaderResourceDescription(URI identifier) {
        super(identifier);
    }

    public void addParent(URI uri) {
        parents.add(uri);
    }

    public void addParents(List<URI> uris) {
        parents.addAll(uris);
    }

    public List<URI> getParents() {
        return Collections.unmodifiableList(parents);
    }

    public void addClassPathUrl(URL url) {
        classPathUrls.add(url);
    }

    public void addClassPathUrls(List<URL> urls) {
        classPathUrls.addAll(urls);
    }

    public List<URL> getClassPathUrls() {
        return Collections.unmodifiableList(classPathUrls);
    }
}
