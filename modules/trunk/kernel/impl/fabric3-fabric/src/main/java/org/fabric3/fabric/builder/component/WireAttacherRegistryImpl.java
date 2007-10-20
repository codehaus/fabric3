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
package org.fabric3.fabric.builder.component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.WireAttacher;
import org.fabric3.spi.builder.component.WireAttacherRegistry;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.wire.Wire;

/**
 * Default implementation of the WireAttacher
 *
 * @version $Rev$ $Date$
 */
public class WireAttacherRegistryImpl implements WireAttacherRegistry {

    Map<Class<?>, WireAttacher<? extends PhysicalWireSourceDefinition,
            ? extends PhysicalWireTargetDefinition>> attachers =
            new ConcurrentHashMap<Class<?>, WireAttacher<? extends PhysicalWireSourceDefinition,
                    ? extends PhysicalWireTargetDefinition>>();

    public <PWSD extends PhysicalWireSourceDefinition, PWTD extends PhysicalWireTargetDefinition>
    void register(Class<?> clazz, WireAttacher<PWSD, PWTD> attacher) {
        attachers.put(clazz, attacher);
    }

    @SuppressWarnings("unchecked")
    public <PWSD extends PhysicalWireSourceDefinition>
    void attachToSource(PWSD source,
                        PhysicalWireTargetDefinition target,
                        Wire wire) throws WiringException {
        Class<?> type = source.getClass();
        WireAttacher attacher = attachers.get(type);
        if (attacher == null) {
            throw new WireAttacherNotFound(type, source.getUri(), wire.getTargetUri());
        }
        attacher.attachToSource(source, target, wire);
    }

    @SuppressWarnings("unchecked")
    public <PWTD extends PhysicalWireTargetDefinition>
    void attachToTarget(PhysicalWireSourceDefinition source,
                        PWTD target,
                        Wire wire) throws WiringException {
        Class<?> type = target.getClass();
        WireAttacher attacher = attachers.get(type);
        if (attacher == null) {
            throw new WireAttacherNotFound(type, target.getUri(), target.getUri());
        }
        attacher.attachToTarget(source, target, wire);
    }
}
