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
package org.fabric3.timer.component.control;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.java.control.JavaGenerationHelper;
import org.fabric3.java.provision.JavaWireSourceDefinition;
import org.fabric3.pojo.instancefactory.InstanceFactoryGenerationHelper;
import org.fabric3.scdl.ServiceContract;
import org.fabric3.spi.generator.ComponentGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.generator.GeneratorRegistry;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalResource;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.physical.PhysicalComponentDefinition;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.policy.Policy;
import org.fabric3.timer.component.provision.TimerComponentDefinition;
import org.fabric3.timer.component.scdl.TimerImplementation;

/**
 * Generates a TimerComponentDefinition from a ComponentDefinition corresponding to a timer component implementation
 *
 * @version $Rev: 4833 $ $Date: 2008-06-20 03:41:57 -0700 (Fri, 20 Jun 2008) $
 */
@EagerInit
public class TimerComponentGenerator implements ComponentGenerator<LogicalComponent<TimerImplementation>> {
    protected final GeneratorRegistry registry;
    private JavaGenerationHelper generationHelper;
    protected final InstanceFactoryGenerationHelper ifHelper;

    public TimerComponentGenerator(@Reference GeneratorRegistry registry,
                                   @Reference JavaGenerationHelper generationHelper,
                                   @Reference InstanceFactoryGenerationHelper ifHelper) {
        this.registry = registry;
        this.generationHelper = generationHelper;
        this.ifHelper = ifHelper;
    }

    @Init
    public void init() {
        registry.register(TimerImplementation.class, this);
    }

    public PhysicalComponentDefinition generate(LogicalComponent<TimerImplementation> component) throws GenerationException {
        TimerComponentDefinition physical = new TimerComponentDefinition();
        generationHelper.generate(component, physical);
        TimerImplementation implementation = component.getDefinition().getImplementation();
        physical.setTriggerType(implementation.getTriggerType());
        physical.setTimeUnit(implementation.getTimeUnit());
        physical.setCronExpression(implementation.getCronExpression());
        physical.setEndTime(implementation.getEndTime());
        physical.setRepeatInterval(implementation.getRepeatInterval());
        physical.setFixedRate(implementation.getFixedRate());
        physical.setFireOnce(implementation.getFireOnce());
        return physical;
    }

    public PhysicalWireSourceDefinition generateWireSource(LogicalComponent<TimerImplementation> source, LogicalReference reference, Policy policy)
            throws GenerationException {
        JavaWireSourceDefinition wireDefinition = new JavaWireSourceDefinition();
        return generationHelper.generateWireSource(source, wireDefinition, reference, policy);
    }

    public PhysicalWireSourceDefinition generateCallbackWireSource(LogicalComponent<TimerImplementation> source,
                                                                   ServiceContract<?> serviceContract,
                                                                   Policy policy) throws GenerationException {
        JavaWireSourceDefinition wireDefinition = new JavaWireSourceDefinition();
        return generationHelper.generateCallbackWireSource(source, wireDefinition, serviceContract, policy);
    }

    public PhysicalWireSourceDefinition generateResourceWireSource(LogicalComponent<TimerImplementation> source, LogicalResource<?> resource)
            throws GenerationException {
        JavaWireSourceDefinition wireDefinition = new JavaWireSourceDefinition();
        return generationHelper.generateResourceWireSource(source, resource, wireDefinition);
    }

    public PhysicalWireTargetDefinition generateWireTarget(LogicalService service, LogicalComponent<TimerImplementation> target, Policy policy)
            throws GenerationException {
        throw new UnsupportedOperationException("Cannot wire to timer components");
    }
}