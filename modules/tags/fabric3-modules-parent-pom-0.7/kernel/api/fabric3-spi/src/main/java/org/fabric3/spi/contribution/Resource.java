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
package org.fabric3.spi.contribution;

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
