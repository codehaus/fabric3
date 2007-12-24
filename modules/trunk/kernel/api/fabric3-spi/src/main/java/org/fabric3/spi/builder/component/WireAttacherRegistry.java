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
package org.fabric3.spi.builder.component;

import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.wire.Wire;

/**
 * A registry for {@link WireAttacher}s
 *
 * @version $Rev$ $Date$
 */
@Deprecated
public interface WireAttacherRegistry {

    <PWSD extends PhysicalWireSourceDefinition,
            PWTD extends PhysicalWireTargetDefinition> void register(Class<?> clazz, WireAttacher<PWSD, PWTD> attacher);

    /**
     * Attaches a wire to a source component.
     *
     * @param source metadata for performing the attach
     * @param target metadata for performing the attach
     * @param wire   the wire
     * @throws WiringException if an exception occurs during the attach operation
     */
    <PWSD extends PhysicalWireSourceDefinition> void attachToSource(PWSD source,
                                                                    PhysicalWireTargetDefinition target,
                                                                    Wire wire)
            throws WiringException;

    /**
     * Attaches a wire to a target component.
     *
     * @param source metadata for performing the attach
     * @param target metadata for performing the attach
     * @param wire   the wire
     * @throws WiringException if an exception occurs during the attach operation
     */
    <PWTD extends PhysicalWireTargetDefinition> void attachToTarget(PhysicalWireSourceDefinition source,
                                                                    PWTD target,
                                                                    Wire wire) throws WiringException;

}
