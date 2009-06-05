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
package org.fabric3.util.graph;

import java.util.List;

/**
 * Performs topological sorts of a directed acyclic graph (DAG).
 *
 * @version $Rev$ $Date$
 */
public interface TopologicalSorter<T> {
    /**
     * Performs a topological sort of the graph.
     *
     * @param dag the DAG to sort
     * @return the total ordered list of vertices.
     * @throws GraphException if a cycle or other error is detected
     */
    List<Vertex<T>> sort(DirectedGraph<T> dag) throws GraphException;

    /**
     * Performs a topological sort of the subgraph reachable from the outgoing edges of the given vertex.
     *
     * @param dag   the DAG to sort
     * @param start the starting vertex.
     * @return the total ordered list of vertice
     * @throws GraphException if a cycle or other error is detected
     */
    List<Vertex<T>> sort(DirectedGraph<T> dag, Vertex<T> start) throws GraphException;

    /**
     * Performs a reverse topological sort of the subgraph reachable from the outgoing edges of the given vertex.
     *
     * @param dag the DAG to sort
     * @return the sorted list of vertices.
     * @throws GraphException if a cycle or other error is detected
     */
    List<Vertex<T>> reverseSort(DirectedGraph<T> dag) throws GraphException;

    /**
     * Performs a topological sort of the subgraph reachable from the outgoing edges of the given vertex.
     *
     * @param dag   the DAG to sort
     * @param start the starting vertex.
     * @return the total ordered list of vertices
     * @throws GraphException if a cycle or other error is detected
     */
    List<Vertex<T>> reverseSort(DirectedGraph<T> dag, Vertex<T> start) throws GraphException;
}
