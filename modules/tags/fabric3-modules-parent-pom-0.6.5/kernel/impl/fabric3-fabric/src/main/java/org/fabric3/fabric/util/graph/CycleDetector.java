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
 * Detects cycles in a directed graph.
 *
 * @version $Rev$ $Date$
 */
public interface CycleDetector<T> {

    /**
     * Determines if a directed graph associated with this detector has cycles.
     *
     * @param graph the graph to check
     * @return true if a directed graph has cycles
     */
    boolean hasCycles(DirectedGraph<T> graph);

    /**
     * Returns the subgraph containing cycles in the graph associated with this detector.
     *
     * @param graph the graph to check
     * @return the subgraph
     */
    DirectedGraph<T> findCycleSubgraph(DirectedGraph<T> graph);

    public List<Cycle<T>> findCycles(DirectedGraph<T> graph);

}
