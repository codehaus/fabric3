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
package org.fabric3.resource.wire;

import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.resource.model.SystemSourcedWireTargetDefinition;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.TargetWireAttacher;
import org.fabric3.spi.builder.component.TargetWireAttacherRegistry;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.wire.Wire;

/**
 * @version $Revision$ $Date$
 */
@EagerInit
public class SystemSourcedResourceWireAttacher implements TargetWireAttacher<SystemSourcedWireTargetDefinition> {
    private final TargetWireAttacherRegistry targetWireAttacherRegistry;

    public SystemSourcedResourceWireAttacher(@Reference TargetWireAttacherRegistry targetWireAttacherRegistry) {
        this.targetWireAttacherRegistry = targetWireAttacherRegistry;
    }

    @Init
    public void start() {
        targetWireAttacherRegistry.register(SystemSourcedWireTargetDefinition.class, this);
    }

    @Destroy
    public void stop() {
        targetWireAttacherRegistry.unregister(SystemSourcedWireTargetDefinition.class, this);
    }

    public void attachToTarget(PhysicalWireSourceDefinition source, SystemSourcedWireTargetDefinition target, Wire wire)
            throws WiringException {
    }

}
