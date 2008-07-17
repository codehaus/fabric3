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
 * A interface for a visitor in the Visitor Pattern.
 *
 * @version $Rev$ $Date$
 */
public interface Visitor<T> {

    /**
     * Perform the visit staring at the given vertex
     *
     * @param start the starting vertex
     * @return true if completed successfully
     */
    public boolean visit(Vertex<T> start);


}

