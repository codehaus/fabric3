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
package org.fabric3.binding.aq.wire;

import org.fabric3.binding.aq.model.physical.AQWireSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.wire.Wire;

/**
 * Bean to hold the Source metatdata
 */
class WireSourceData {

    private final AQWireSourceDefinition sourceDefinition;
    private final PhysicalWireTargetDefinition targetDefinition;
    private final Wire wire;

    /**
     * Construct
     * 
     * @param sourceDefinition
     * @param targetDefinition
     * @param wire
     */
    WireSourceData(final AQWireSourceDefinition sourceDefinition, final PhysicalWireTargetDefinition targetDefinition, final Wire wire) {
        this.sourceDefinition = sourceDefinition;
        this.targetDefinition = targetDefinition;
        this.wire = wire;
    }

    /**
     * Returns the {@link AQWireSourceDefinition}
     * 
     * @return sourceDefinition
     */
    AQWireSourceDefinition getSourceDefinition() {
        return sourceDefinition;
    }

    /**
     * Returns the {@link PhysicalWireTargetDefinition}
     * 
     * @return targetDefinition
     */
    PhysicalWireTargetDefinition getTargetDefinition() {
        return targetDefinition;
    }

    /**
     * Returns the {@link Wire}
     * 
     * @return
     */
    Wire getWire() {
        return wire;
    }    
}
