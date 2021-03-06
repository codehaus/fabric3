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
package org.fabric3.util.graph;

import java.util.List;

/**
 * Conducts a depth first graph traversal, returning an ordered list of visited vertices.
 *
 * @version $Rev$ $Date$
 */
public interface DepthFirstTraverser<T> {

    /**
     * Traverse the graph starting at the given vertex.
     *
     * @param graph the graph
     * @param start the starting vertex
     * @return the vertices sorted depth-first
     */
    List<Vertex<T>> traverse(DirectedGraph<T> graph, Vertex<T> start);

    /**
     * Traverse the graph starting at a vertex and ending at another.
     *
     * @param graph the graph
     * @param start the starting vertex
     * @param end   the ending vertex
     * @return the vertices sorted depth-first or an empty list if no path betweent the vertices exists
     */
    List<Vertex<T>> traversePath(DirectedGraph<T> graph, Vertex<T> start, Vertex<T> end);

}
