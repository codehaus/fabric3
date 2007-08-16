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
package org.fabric3.runtime.standalone.host.implementation.launched;

import java.lang.reflect.Member;
import java.net.URI;
import java.util.Map;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.fabric.implementation.java.JavaComponentDefinition;
import org.fabric3.fabric.implementation.java.JavaWireSourceDefinition;
import org.fabric3.fabric.implementation.java.JavaWireTargetDefinition;
import org.fabric3.pojo.instancefactory.InjectionSiteMapping;
import org.fabric3.pojo.instancefactory.InstanceFactoryDefinition;
import org.fabric3.pojo.instancefactory.MemberSite;
import org.fabric3.pojo.scdl.ConstructorDefinition;
import org.fabric3.pojo.scdl.JavaMappedProperty;
import org.fabric3.pojo.scdl.JavaMappedReference;
import org.fabric3.pojo.scdl.JavaMappedService;
import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.spi.generator.ComponentGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.generator.GeneratorContext;
import org.fabric3.spi.generator.GeneratorRegistry;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.instance.ValueSource;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.scdl.ComponentDefinition;
import org.fabric3.scdl.Property;

/**
 * @version $Rev$ $Date$
 */
@EagerInit
public class LaunchedComponentGenerator implements ComponentGenerator<LogicalComponent<Launched>> {

    public LaunchedComponentGenerator(@Reference GeneratorRegistry registry) {
        registry.register(Launched.class, this);
    }

    @SuppressWarnings({"unchecked"})
    public void generate(LogicalComponent<Launched> component, GeneratorContext context) {
        ComponentDefinition<Launched> definition = component.getDefinition();
        Launched implementation = definition.getImplementation();
        // TODO not a safe cast
        PojoComponentType type = implementation.getComponentType();
        JavaComponentDefinition pDefinition = new JavaComponentDefinition();
        URI componentId = component.getUri();
        pDefinition.setGroupId(componentId.resolve("."));
        pDefinition.setComponentId(componentId);
        // set the classloader id temporarily until multiparent classloading is in palce
        pDefinition.setClassLoaderId(URI.create("sca://./applicationClassLoader"));
        pDefinition.setScope(type.getImplementationScope());
        // TODO get classloader id
        InstanceFactoryDefinition providerDefinition = new InstanceFactoryDefinition();
        providerDefinition.setInitMethod(type.getInitMethod());
        providerDefinition.setDestroyMethod(type.getDestroyMethod());

        // JFM FIXME seems hacky and add to JavaPCDG
        Integer level = definition.getInitLevel();
        if (level == null) {
            pDefinition.setInitLevel(type.getInitLevel());
        } else {
            pDefinition.setInitLevel(level);
        }
        providerDefinition.setImplementationClass(implementation.getImplementationClass());
        processConstructorArguments(type.getConstructorDefinition(), providerDefinition);
        processConstructorSites(type, providerDefinition);
        processReferenceSites(type, providerDefinition);
        // TODO process properties
        pDefinition.setInstanceFactoryProviderDefinition(providerDefinition);
        context.getPhysicalChangeSet().addComponentDefinition(pDefinition);
    }

    public PhysicalWireSourceDefinition generateWireSource(LogicalComponent<Launched> source,
                                                           LogicalReference reference,
                                                           boolean optimizable, GeneratorContext context)
            throws GenerationException {
        JavaWireSourceDefinition wireDefinition = new JavaWireSourceDefinition();
        wireDefinition.setUri(reference.getUri());
        wireDefinition.setOptimizable(true);
        return wireDefinition;
    }

    public PhysicalWireTargetDefinition generateWireTarget(LogicalService service, LogicalComponent<Launched> logical,
                                                           GeneratorContext context) throws GenerationException {
        JavaWireTargetDefinition wireDefinition = new JavaWireTargetDefinition();
        wireDefinition.setUri(service.getUri());
        return wireDefinition;
    }

    /**
     * Creates InjectionSources for constructor parameters for the component implementation
     *
     * @param type               the component type corresponding to the implementation
     * @param providerDefinition the instance factory provider definition
     */
    private void processConstructorSites(PojoComponentType type,
                                         InstanceFactoryDefinition providerDefinition) {
        Map<String, JavaMappedReference> references = type.getReferences();
        Map<String, JavaMappedProperty<?>> properties = type.getProperties();
        Map<String, JavaMappedService> services = type.getServices();

        // process constructor injectors
        ConstructorDefinition<?> ctorDef = type.getConstructorDefinition();
        for (String name : ctorDef.getInjectionNames()) {
            JavaMappedReference reference = references.get(name);
            if (reference != null) {
                ValueSource source = new ValueSource(ValueSource.ValueSourceType.REFERENCE, name);
                providerDefinition.addCdiSource(source);
                continue;
            }
            Property<?> property = properties.get(name);
            if (property != null) {
                ValueSource source = new ValueSource(ValueSource.ValueSourceType.PROPERTY, name);
                providerDefinition.addCdiSource(source);
                continue;
            }
            JavaMappedService service = services.get(name);
            if (service != null) {
                // SPEC The SCA spec does not specifically allow this yet -  submit an enhnacement request
                ValueSource source = new ValueSource(ValueSource.ValueSourceType.SERVICE, name);
                providerDefinition.addCdiSource(source);
                continue;
            }
            throw new AssertionError();
        }

    }

    /**
     * Creates InjectionSiteMappings for references declared by the component implementation
     *
     * @param type               the component type corresponding to the implementation
     * @param providerDefinition the instance factory provider definition
     */
    private void processReferenceSites(PojoComponentType type,
                                       InstanceFactoryDefinition providerDefinition) {
        Map<String, JavaMappedReference> references = type.getReferences();
        for (Map.Entry<String, JavaMappedReference> entry : references.entrySet()) {
            JavaMappedReference reference = entry.getValue();
            Member member = reference.getMember();
            if (member == null) {
                // JFM this is dubious, the reference is mapped to a constructor so skip processing
                // ImplementationProcessorService does not set the member type to a ctor when creating the ref
                continue;
            }
            ValueSource source = new ValueSource(ValueSource.ValueSourceType.REFERENCE, entry.getKey());
            MemberSite memberSite = new MemberSite(member);
            
            InjectionSiteMapping mapping = new InjectionSiteMapping();
            mapping.setSource(source);
            mapping.setSite(memberSite);
            providerDefinition.addInjectionSite(mapping);
        }
    }

    /**
     * Adds the constructor parameter types to the provider definition
     *
     * @param ctorDef            the constructor definition
     * @param providerDefinition the provider definition
     */
    private void processConstructorArguments(ConstructorDefinition<?> ctorDef,
                                             InstanceFactoryDefinition providerDefinition) {
        for (Class<?> type : ctorDef.getConstructor().getParameterTypes()) {
            providerDefinition.addConstructorArgument(type.getName());
        }
    }


}
