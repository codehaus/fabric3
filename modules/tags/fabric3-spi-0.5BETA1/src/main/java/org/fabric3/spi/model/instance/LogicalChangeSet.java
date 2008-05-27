/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
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
package org.fabric3.spi.model.instance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a logical changeset containing any newly added components and wires as 
 * a result of a new contribution.
 * 
 * @version $Revision$ $Date$
 */
public class LogicalChangeSet {
    
    private final List<LogicalComponent<?>> newComponents = new ArrayList<LogicalComponent<?>>();
    private final List<LogicalWire> newWires = new ArrayList<LogicalWire>();
    
    /**
     * Initializes the newly added components and wires as a result of the new contribution.
     * 
     * @param components Newly added components.
     * @param wires Newly added wires.
     */
    public LogicalChangeSet(List<LogicalComponent<?>> components, List<LogicalWire> wires) {
        newComponents.addAll(components);
        newWires.addAll(wires);
    }

    /**
     * Gets any newly added components.
     * 
     * @return Newly added components at domain level as a result of the new contribution.
     */
    public List<LogicalComponent<?>> getNewComponents() {
        return Collections.unmodifiableList(newComponents);
    }

    /**
     * Gets any newly added wires.
     * 
     * @return Newly added wires at the domain level as a result of the new contribution.
     */
    public List<LogicalWire> getNewWires() {
        return Collections.unmodifiableList(newWires);
    }

}
