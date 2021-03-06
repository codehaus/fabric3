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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Default implementation of a depth first search.
 *
 * @version $Rev$ $Date$
 */
public class DepthFirstTraverserImpl<T> implements DepthFirstTraverser<T> {

    public List<Vertex<T>> traverse(DirectedGraph<T> graph, Vertex<T> start) {
        return traverse(graph, start, new TrueVisitor<T>());
    }

    public List<Vertex<T>> traversePath(DirectedGraph<T> graph, Vertex<T> start, Vertex<T> end) {
        TerminatingVisitor<T> visitor = new TerminatingVisitor<T>(end);
        List<Vertex<T>> path = traverse(graph, start, visitor);
        if (visitor.wasFound()) {
            return path;
        }
        return Collections.emptyList();
    }

    private List<Vertex<T>> traverse(DirectedGraph<T> graph, Vertex<T> start, Visitor<T> visitor) {
        List<Vertex<T>> visited = new ArrayList<Vertex<T>>();
        List<Vertex<T>> stack = new ArrayList<Vertex<T>>();
        Set<Vertex<T>> seen = new HashSet<Vertex<T>>(visited);
        stack.add(start);
        seen.add(start);
        do {
            // mark as visited
            Vertex<T> next = stack.remove(stack.size() - 1);
            visited.add(next);
            if (!visitor.visit(next)) {
                return visited;
            }

            // add all non-visited adjacent vertices to the stack
            Set<Vertex<T>> adjacentVertices = graph.getAdjacentVertices(next);
            for (Vertex<T> v : adjacentVertices) {
                seen.add(v);
                stack.add(v);
            }

        } while (!stack.isEmpty());
        return visited;
    }


}
