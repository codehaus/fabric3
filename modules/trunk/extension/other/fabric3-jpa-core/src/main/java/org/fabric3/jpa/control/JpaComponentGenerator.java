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
package org.fabric3.jpa.control;

import org.fabric3.jpa.provision.JpaComponentDefinition;
import org.fabric3.jpa.provision.JpaWireTargetDefinition;
import org.fabric3.jpa.scdl.JpaImplementation;
import org.fabric3.scdl.ServiceContract;
import org.fabric3.spi.generator.ComponentGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalResource;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.physical.PhysicalComponentDefinition;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.policy.Policy;

/**
 *
 * @version $Revision$ $Date$
 */
public class JpaComponentGenerator implements ComponentGenerator<LogicalComponent<JpaImplementation>> {

    public PhysicalComponentDefinition generate(LogicalComponent<JpaImplementation> component) throws GenerationException {
        // TODO Get the scope and persistence unit
        return new JpaComponentDefinition();
    }

    public PhysicalWireTargetDefinition generateWireTarget(LogicalService service, LogicalComponent<JpaImplementation> target, Policy policy)
            throws GenerationException {
        return new JpaWireTargetDefinition();
    }

    public PhysicalWireSourceDefinition generateCallbackWireSource(LogicalComponent<JpaImplementation> source, ServiceContract<?> serviceContract,
            Policy policy) throws GenerationException {
        throw new UnsupportedOperationException();
    }

    public PhysicalWireSourceDefinition generateResourceWireSource(LogicalComponent<JpaImplementation> source, LogicalResource<?> resource)
            throws GenerationException {
        throw new UnsupportedOperationException();
    }

    public PhysicalWireSourceDefinition generateWireSource(LogicalComponent<JpaImplementation> source, LogicalReference reference, Policy policy)
            throws GenerationException {
        throw new UnsupportedOperationException();
    }

}
