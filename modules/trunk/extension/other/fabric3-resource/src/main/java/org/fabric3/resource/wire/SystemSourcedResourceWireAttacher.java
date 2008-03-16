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

import java.net.URI;

import org.osoa.sca.annotations.Reference;

import org.fabric3.resource.model.SystemSourcedWireTargetDefinition;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.TargetWireAttacher;
import org.fabric3.spi.component.AtomicComponent;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.runtime.component.ComponentManager;
import org.fabric3.spi.util.UriHelper;
import org.fabric3.spi.wire.Wire;

/**
 * @version $Revision$ $Date$
 */
public class SystemSourcedResourceWireAttacher implements TargetWireAttacher<SystemSourcedWireTargetDefinition> {
    private final ComponentManager manager;

    public SystemSourcedResourceWireAttacher(@Reference ComponentManager manager) {
        this.manager = manager;
    }

    public void attachToTarget(PhysicalWireSourceDefinition source, SystemSourcedWireTargetDefinition target, Wire wire)
            throws WiringException {
        throw new AssertionError();
    }

    public ObjectFactory<?> createObjectFactory(SystemSourcedWireTargetDefinition target) throws WiringException {
        URI targetId = UriHelper.getDefragmentedName(target.getUri());
        AtomicComponent<?> targetComponent = (AtomicComponent<?>) manager.getComponent(targetId);
        return targetComponent.createObjectFactory();
    }
}
