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
