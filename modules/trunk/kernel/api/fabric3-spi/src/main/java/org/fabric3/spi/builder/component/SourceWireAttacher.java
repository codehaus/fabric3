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
     * Attaches an ObjectFactory to a source component.
     *
     * @param source        the definition of the component reference to attach to
     * @param objectFactory an ObjectFactory that can produce values compatible with the reference
     * @throws WiringException if an exception occurs during the attach operation
     */
    void attachObjectFactory(PWSD source, ObjectFactory<?> objectFactory) throws WiringException;
}
