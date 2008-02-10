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
package org.fabric3.pojo.instancefactory;

import java.lang.reflect.Method;

import org.fabric3.pojo.implementation.PojoComponentDefinition;
import org.fabric3.pojo.scdl.ConstructorDefinition;
import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.scdl.ComponentDefinition;
import org.fabric3.scdl.Implementation;
import org.fabric3.scdl.Signature;
import org.fabric3.spi.model.instance.LogicalComponent;

/**
 * @version $Rev$ $Date$
 */
public interface InstanceFactoryGenerationHelper {
    Integer getInitLevel(ComponentDefinition<?> definition, PojoComponentType type);

    Signature getSignature(Method method);

    void processInjectionSites(LogicalComponent<? extends Implementation<PojoComponentType>> component, InstanceFactoryDefinition providerDefinition);

    /**
     * Creates InjectionSources for constructor parameters for the component implementation
     *
     * @param type               the component type corresponding to the implementation
     * @param providerDefinition the instance factory provider definition
     */
    void processConstructorSites(PojoComponentType type, InstanceFactoryDefinition providerDefinition);

    /**
     * Creates InjectionSiteMappings for references declared by the component implementation
     *
     * @param component          the component corresponding to the implementation
     * @param providerDefinition the instance factory provider definition
     */
    void processReferenceSites(LogicalComponent<? extends Implementation<PojoComponentType>> component, InstanceFactoryDefinition providerDefinition);

    /**
     * Creates InjectionSiteMappings for properties declared by the component implementation
     *
     * @param component          the component corresponding to the implementation
     * @param providerDefinition the instance factory provider definition
     */
    void processPropertySites(LogicalComponent<? extends Implementation<PojoComponentType>> component, InstanceFactoryDefinition providerDefinition);

    /**
     * Creates InjectionSiteMappings for resources declared by the component implementation
     *
     * @param component          the component corresponding to the implementation
     * @param providerDefinition the instance factory provider definition
     */
    void processResourceSites(LogicalComponent<? extends Implementation<PojoComponentType>> component, InstanceFactoryDefinition providerDefinition);

    /**
     * Adds the constructor parameter types to the provider definition
     *
     * @param ctorDef            the constructor definition
     * @param providerDefinition the provider definition
     */
    void processConstructorArguments(ConstructorDefinition<?> ctorDef, InstanceFactoryDefinition providerDefinition);

    /**
     * Set the actual values of the physical properties.
     *
     * @param component the component corresponding to the implementation
     * @param physical  the physical component whose properties should be set
     */
    void processPropertyValues(LogicalComponent<?> component, PojoComponentDefinition physical);
}
