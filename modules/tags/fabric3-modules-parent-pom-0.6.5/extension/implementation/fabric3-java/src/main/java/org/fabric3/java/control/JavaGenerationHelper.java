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
package org.fabric3.java.control;

import org.fabric3.java.provision.JavaComponentDefinition;
import org.fabric3.java.provision.JavaWireSourceDefinition;
import org.fabric3.java.provision.JavaWireTargetDefinition;
import org.fabric3.java.scdl.JavaImplementation;
import org.fabric3.scdl.ServiceContract;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalResource;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.policy.Policy;

/**
 * Handles generation operations for Java components and specialized subtypes.
 *
 * @version $Revision$ $Date$
 */
public interface JavaGenerationHelper {

    JavaComponentDefinition generate(LogicalComponent<? extends JavaImplementation> component, JavaComponentDefinition physical)
            throws GenerationException;

    PhysicalWireSourceDefinition generateWireSource(LogicalComponent<? extends JavaImplementation> source,
                                                    JavaWireSourceDefinition wireDefinition,
                                                    LogicalReference reference,
                                                    Policy policy) throws GenerationException;

    PhysicalWireSourceDefinition generateCallbackWireSource(LogicalComponent<? extends JavaImplementation> source,
                                                            JavaWireSourceDefinition wireDefinition,
                                                            ServiceContract<?> serviceContract,
                                                            Policy policy) throws GenerationException;

    PhysicalWireSourceDefinition generateResourceWireSource(LogicalComponent<? extends JavaImplementation> source,
                                                            LogicalResource<?> resource,
                                                            JavaWireSourceDefinition wireDefinition) throws GenerationException;

    PhysicalWireTargetDefinition generateWireTarget(LogicalService service,
                                                    LogicalComponent<? extends JavaImplementation> target,
                                                    JavaWireTargetDefinition wireDefinition,
                                                    Policy policy) throws GenerationException;
}
