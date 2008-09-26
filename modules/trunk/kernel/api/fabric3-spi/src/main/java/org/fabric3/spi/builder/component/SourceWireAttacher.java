/*
 * Fabric3
 * Copyright � 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the �License�), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an �as is� basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 * --- Original Apache License ---
 *
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

import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.wire.Wire;

/**
 * Component that handles attachment of a wire at the source side of the invocation chain.
 * <p/>
 * Implementations attach physical wires to component implementations so that the implementation can invoke other components. These may be for
 * references or for callbacks.
 *
 * @version $Rev$ $Date$
 */
public interface SourceWireAttacher<PWSD extends PhysicalWireSourceDefinition> {
    /**
     * Attaches a wire to a source component or and incoming binding.
     *
     * @param source metadata for performing the attach
     * @param target metadata for performing the attach
     * @param wire   the wire
     * @throws WiringException if an exception occurs during the attach operation
     */
    void attachToSource(PWSD source, PhysicalWireTargetDefinition target, Wire wire) throws WiringException;


    /**
     * Detach a wire from a source component
     * @param source
     * @param target
     * @param wire
     */
    void detachFromSource(PWSD source, PhysicalWireTargetDefinition target, Wire wire) throws WiringException;
    /**
     * Attaches an ObjectFactory to a source component.
     *
     * @param source        the definition of the component reference to attach to
     * @param objectFactory an ObjectFactory that can produce values compatible with the reference
     * @param target    the target definition for the wire
     * @throws WiringException if an exception occurs during the attach operation
     */
    void attachObjectFactory(PWSD source, ObjectFactory<?> objectFactory, PhysicalWireTargetDefinition target) throws WiringException;
}
