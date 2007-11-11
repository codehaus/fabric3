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
package org.fabric3.mock;

import org.fabric3.spi.builder.component.WireAttacher;
import org.fabric3.spi.builder.component.WireAttacherRegistry;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.wire.Wire;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

/**
 * @version $Revision$ $Date$
 */
@EagerInit
public class MockWireAttacher implements WireAttacher<PhysicalWireSourceDefinition, MockWireTargetDefinition> {
    
    private final WireAttacherRegistry wireAttacherRegistry;
    
    public MockWireAttacher(@Reference WireAttacherRegistry wireAttacherRegistry) {
        this.wireAttacherRegistry = wireAttacherRegistry;
    }
    
    @Init
    public void init() {
        wireAttacherRegistry.register(MockWireTargetDefinition.class, this);
    }

    public void attachToSource(PhysicalWireSourceDefinition wireSourceDefinition, 
                               PhysicalWireTargetDefinition wireTargetDefinition, 
                               Wire wire) {
        throw new UnsupportedOperationException("Mock components cant be sources for wires");
    }

    public void attachToTarget(PhysicalWireSourceDefinition wireSourceDefinition, 
                               MockWireTargetDefinition wireTargetDefinition, 
                               Wire wire) {
    }

}
