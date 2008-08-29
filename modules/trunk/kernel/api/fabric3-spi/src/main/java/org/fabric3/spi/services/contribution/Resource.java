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
package org.fabric3.spi.services.contribution;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.io.Serializable;

/**
 * Represents a resource in a contribution such as a WSDL file or Composite definition
 *
 * @version $Rev$ $Date$
 */
public class Resource implements Serializable {
    private static final long serialVersionUID = 4291622973495594302L;
    private List<ResourceElement<?, ?>> elements = new ArrayList<ResourceElement<?, ?>>();
    private URL url;
    private String contentType;
    private boolean processed;

    public Resource(URL url, String contentType) {
        this.url = url;
        this.contentType = contentType;
    }

    /**
     * Returns the resource content type
     *
     * @return the resource content type
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Returns a derefereceable URL to the resource.
     *
     * @return a derefereceable URL to the resource
     */
    public URL getUrl() {
        return url;
    }

    /**
     * Adds a resource element.
     *
     * @param element the resourceElement
     */
    public void addResourceElement(ResourceElement<?, ?> element) {
        elements.add(element);
    }

    public void addResourceElements(Collection<ResourceElement<?, ?>> elements) {
        elements.addAll(elements);
    }

    /**
     * Returns a map of resource elements keyed by their symbol.
     *
     * @return the map of resource elements
     */
    public List<ResourceElement<?, ?>> getResourceElements() {
        return elements;
    }

    /**
     * Returns true if the resource has been fully processed.
     *
     * @return true if the resource has been fully processed
     */
    public boolean isProcessed() {
        return processed;
    }

    /**
     * Sets if the resource has been processed.
     *
     * @param processed if the resource has been processed
     */
    public void setProcessed(boolean processed) {
        this.processed = processed;
    }
}
