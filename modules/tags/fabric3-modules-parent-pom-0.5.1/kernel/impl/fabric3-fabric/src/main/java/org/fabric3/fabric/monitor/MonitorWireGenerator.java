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
package org.fabric3.fabric.monitor;

import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.scdl.MonitorResource;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.generator.GeneratorRegistry;
import org.fabric3.spi.generator.ResourceWireGenerator;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalResource;

/**
 * @version $Rev$ $Date$
 */
@EagerInit
public class MonitorWireGenerator implements ResourceWireGenerator<MonitorWireTargetDefinition, MonitorResource> {

    private final GeneratorRegistry registry;

    public MonitorWireGenerator(@Reference(name = "registry")GeneratorRegistry registry) {
        this.registry = registry;
    }

    @Init
    public void init() {
        registry.register(MonitorResource.class, this);
    }

    @Destroy
    public void destroy() {
        registry.unregister(MonitorResource.class, this);
    }

    public MonitorWireTargetDefinition generateWireTargetDefinition(LogicalResource<MonitorResource> resource) throws GenerationException {

        LogicalComponent<?> component = resource.getParent();

        MonitorWireTargetDefinition definition = new MonitorWireTargetDefinition();
        definition.setMonitorType(resource.getResourceDefinition().getServiceContract().getQualifiedInterfaceName());
        definition.setUri(component.getUri());
        definition.setOptimizable(true);
        definition.setClassLoaderId(component.getClassLoaderId());

        return definition;
    }
}
