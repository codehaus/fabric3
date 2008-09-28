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
package org.fabric3.fabric.util.graph;

import java.util.List;

/**
 * Represents a cycle in a directed graph
 *
 * @version $Rev$ $Date$
 */
public class Cycle<T> {
    private List<Vertex<T>> originPath;
    private List<Vertex<T>> backPath;

    /**
     * Returns the list of vertices from the cycle origin to the endpoint.
     *
     * @return the list of vertices from the cycle origin to the endpoint
     */
    public List<Vertex<T>> getOriginPath() {
        return originPath;
    }

    /**
     * Sets the list of vertices from the cycle origin to the endpoint.
     *
     * @param originPath the list of vertices from the cycle origin to the endpoint
     */
    public void setOriginPath(List<Vertex<T>> originPath) {
        this.originPath = originPath;
    }

    /**
     * Returns the list of vertices from the cycle endpoint back to the origin.
     *
     * @return the the list of vertices from the cycle endpoint back to the origin
     */
    public List<Vertex<T>> getBackPath() {
        return backPath;
    }

    /**
     * Sets the list of vertices from the cycle endpoint back to the origin.
     *
     * @param backPath the the list of vertices from the cycle endpoint back to the origin
     */
    public void setBackPath(List<Vertex<T>> backPath) {
        this.backPath = backPath;
    }
}
