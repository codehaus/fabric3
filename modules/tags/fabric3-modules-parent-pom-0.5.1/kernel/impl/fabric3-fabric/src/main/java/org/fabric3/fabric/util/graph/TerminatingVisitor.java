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

/**
 * Walks the graph until the specified vertex is reached.
 *
 * @version $Rev$ $Date$
 */

public class TerminatingVisitor<T> implements Visitor<T> {
    private Vertex<T> stopVertex;
    private boolean found;

    /**
     * Constructor.
     *
     * @param vertex vertex to stop at
     */
    public TerminatingVisitor(Vertex<T> vertex) {
        this.stopVertex = vertex;
    }

    /**
     * Returns true if the specified vertex was reached during the traversal.
     *
     * @return true if the specified vertex was reached during the traversal
     */
    public boolean wasFound() {
        return found;
    }

    public boolean visit(Vertex<T> vertex) {
        if (vertex == stopVertex) {
            found = true;
            return false;
        } else {
            return true;
        }
    }

}
