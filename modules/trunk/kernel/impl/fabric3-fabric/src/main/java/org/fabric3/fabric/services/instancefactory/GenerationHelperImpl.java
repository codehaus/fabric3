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
package org.fabric3.fabric.services.instancefactory;

import java.lang.reflect.Method;
import java.util.Map;

import org.w3c.dom.Document;

import org.fabric3.pojo.implementation.PojoComponentDefinition;
import org.fabric3.pojo.instancefactory.InstanceFactoryDefinition;
import org.fabric3.pojo.instancefactory.InstanceFactoryGenerationHelper;
import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.scdl.ComponentDefinition;
import org.fabric3.scdl.Implementation;
import org.fabric3.scdl.InjectionSite;
import org.fabric3.scdl.InjectionSiteMapping;
import org.fabric3.scdl.Signature;
import org.fabric3.scdl.ValueSource;
import static org.fabric3.scdl.ValueSource.ValueSourceType.CONTEXT;
import org.fabric3.spi.model.instance.LogicalComponent;

/**
 * @version $Rev$ $Date$
 */
public class GenerationHelperImpl implements InstanceFactoryGenerationHelper {

    public Integer getInitLevel(ComponentDefinition<?> definition, PojoComponentType type) {
        Integer initLevel = definition.getInitLevel();
        if (initLevel == null) {
            initLevel = type.getInitLevel();
        }
        return initLevel;
    }

    public Signature getSignature(Method method) {
        return method == null ? null : new Signature(method);
    }

    public void processInjectionSites(LogicalComponent<? extends Implementation<PojoComponentType>> component,
                                      InstanceFactoryDefinition providerDefinition) {

        Implementation<PojoComponentType> implementation = component.getDefinition().getImplementation();
        PojoComponentType type = implementation.getComponentType();
        Map<ValueSource, InjectionSite> mappings = type.getInjectionMappings();
        for (Map.Entry<ValueSource, InjectionSite> entry : mappings.entrySet()) {
            ValueSource source = entry.getKey();
            InjectionSite site = entry.getValue();
            addMapping(providerDefinition, source, site);
        }
        processContextSites(component, providerDefinition);
    }


    public void processContextSites(LogicalComponent<? extends Implementation<PojoComponentType>> component,
                                    InstanceFactoryDefinition providerDefinition) {

        Implementation<PojoComponentType> implementation = component.getDefinition().getImplementation();
        PojoComponentType type = implementation.getComponentType();
        InjectionSite componentContextSite = type.getComponentContextMember();
        if (componentContextSite != null) {
            addMapping(providerDefinition, new ValueSource(CONTEXT, "ComponentContext"), componentContextSite);
        }
        InjectionSite requestContextSite = type.getRequestContextMember();
        if (requestContextSite != null) {
            addMapping(providerDefinition, new ValueSource(CONTEXT, "RequestContext"), requestContextSite);
        }
    }

    public void processPropertyValues(LogicalComponent<?> component, PojoComponentDefinition physical) {
        for (Map.Entry<String, Document> entry : component.getPropertyValues().entrySet()) {
            String name = entry.getKey();
            Document value = entry.getValue();
            if (value != null) {
                physical.setPropertyValue(name, value);
            }
        }
    }

    private void addMapping(InstanceFactoryDefinition providerDefinition, ValueSource source, InjectionSite injectionSite) {
        InjectionSiteMapping mapping = new InjectionSiteMapping();
        mapping.setSource(source);
        mapping.setSite(injectionSite);
        providerDefinition.addInjectionSite(mapping);
    }
}
