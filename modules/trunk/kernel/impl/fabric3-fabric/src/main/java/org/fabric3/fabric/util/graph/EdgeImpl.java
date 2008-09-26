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

/**
 * Represents a directed edge in a graph.
 *
 * @version $Rev$ $Date$
 */
public class EdgeImpl<T> implements Edge<T> {
    private Vertex<T> source;
    private Vertex<T> sink;

    public EdgeImpl(Vertex<T> source, Vertex<T> sink) {
        this.source = source;
        this.sink = sink;
    }

    public Vertex<T> getSource() {
        return source;
    }

    public Vertex<T> getSink() {
        return sink;
    }

    public Vertex<T> getOppositeVertex(Vertex v) {
        if (this.source == v) {
            return this.sink;
        } else if (this.sink == v) {
            return this.source;
        } else {
            throw new AssertionError();
        }
    }
}

