/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.fabric.generator.wire;

import java.net.URI;

import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalResource;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.physical.PhysicalWireDefinition;

/**
 * Generates physical wire definitions to provision a logical wire. The number of physical wires generated from a logical wire will vary. A
 * unidirectional wire (i.e. no callback) to a collocated target service will generate one physical wire. A bidirectional wire (i.e. with a callback)
 * to a collocated service will generate two physical wires. A unidirecitonal wire to a remote service offered by an SCA component will generate two
 * physical wires:
 * <pre>
 * <ul>
 * <li>One from the source reference to the transport
 * <li>One from the transport on the target runtime to the target service.
 * </ul>
 * </pre>
 * Likewise, a bidirectional wire to a remote service offered by an SCA component will generate four wires:
 * <pre>
 * <ul>
 * <li>One from the source reference to the transport
 * <li>One from the transport on the target runtime to the target service.
 * <li>One from the callback site on the target to the transport
 * <li>One from the transport on the source runtime to the callback service.
 * </ul>
 * </pre>
 *
 * @version $Revision$ $Date$
 */
public interface PhysicalWireGenerator {

    /**
     * Generates a PhysicalWireDefinition for the resource.
     *
     * @param resource the resource
     * @return the physical wire definition
     * @throws GenerationException if an error ocurrs during generation
     */
    PhysicalWireDefinition generateResourceWire(LogicalResource<?> resource) throws GenerationException;


    /**
     * Generates a PhysicalWireDefinition for a bound service.
     *
     * @param service     the service
     * @param binding     the service binding
     * @param callbackUri the callback URI associated with this wire or null if the service is unidirectional
     * @return the physical wire definition.
     * @throws GenerationException if an error ocurrs during generation
     */
    PhysicalWireDefinition generateBoundServiceWire(LogicalService service, LogicalBinding<?> binding, URI callbackUri) throws GenerationException;

    /**
     * Generates a PhysicalWireDefinition for a bound reference.
     *
     * @param reference the reference
     * @param binding   the reference binding
     * @return the physical wire definition.
     * @throws GenerationException if an error ocurrs during generation
     */
    PhysicalWireDefinition generateBoundReferenceWire(LogicalReference reference, LogicalBinding<?> binding) throws GenerationException;

    /**
     * Generates a PhysicalWireDefinition for callback wire for a bound reference
     *
     * @param reference the reference
     * @param binding   the callback binding
     * @return the physical wire definition.
     * @throws GenerationException if an error ocurrs during generation
     */
    PhysicalWireDefinition generateBoundCallbackRerenceWire(LogicalReference reference, LogicalBinding<?> binding) throws GenerationException;

    /**
     * Generates a PhysicalWireDefinition for callback wire from a component to the callback service provided by a forward service
     *
     * @param service the logical service which provides the callback service
     * @param binding the callback binding
     * @return the physical wire definition.
     * @throws GenerationException if an error ocurrs during generation
     */
    PhysicalWireDefinition generateBoundCallbackServiceWire(LogicalService service, LogicalBinding<?> binding) throws GenerationException;

    /**
     * Generates a PhysicalWireDefinition for a wire between collocated components.
     *
     * @param reference the source reference of the wire
     * @param service   the target service of the wire
     * @return the physical wire definition.
     * @throws GenerationException if an error ocurrs during generation
     */
    PhysicalWireDefinition generateCollocatedWire(LogicalReference reference, LogicalService service) throws GenerationException;

    /**
     * Generates a PhysicalWireDefinition for a callback wire between collocated components.
     *
     * @param component the component originating the callback, which is the target of the forward wire
     * @param reference the reference the forward wire is injected on
     * @return the physical wire definition.
     * @throws GenerationException if an error ocurrs during generation
     * @FIXME JFM passing in the LogicalReference doesn't seem right but the policy generation appears to need it. Look to remove.
     */
    public PhysicalWireDefinition generateCollocatedCallbackWire(LogicalComponent<?> component, LogicalReference reference)
            throws GenerationException;

}
