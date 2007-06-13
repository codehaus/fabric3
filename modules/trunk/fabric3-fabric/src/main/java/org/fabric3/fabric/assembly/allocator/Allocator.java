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
package org.fabric3.fabric.assembly.allocator;

import java.util.Map;

import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.topology.RuntimeInfo;

/**
 * Allocates a component to a service node.
 *
 * @version $Rev$ $Date$
 */
public interface Allocator {

    /**
     * Performs the allocation. Composites are recursed and their children are allocated.
     *
     * @param infos     the set of service nodes available for allocation
     * @param component the component to allocate
     * @throws AllocationException if an error during allocation occurs
     */
    void allocate(Map<String, RuntimeInfo> infos, LogicalComponent<?> component) throws AllocationException;
}
