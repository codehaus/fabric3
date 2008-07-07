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
