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
import org.fabric3.pojo.instancefactory.InjectionSiteMapping;
import org.fabric3.pojo.instancefactory.InstanceFactoryDefinition;
import org.fabric3.pojo.instancefactory.InstanceFactoryGenerationHelper;
import org.fabric3.pojo.instancefactory.Signature;
import org.fabric3.pojo.scdl.ConstructorDefinition;
import org.fabric3.pojo.scdl.JavaMappedProperty;
import org.fabric3.pojo.scdl.JavaMappedReference;
import org.fabric3.pojo.scdl.JavaMappedResource;
import org.fabric3.pojo.scdl.JavaMappedService;
import org.fabric3.pojo.scdl.MemberSite;
import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.scdl.ComponentDefinition;
import org.fabric3.scdl.Implementation;
import org.fabric3.scdl.Property;
import org.fabric3.scdl.PropertyValue;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalResource;
import org.fabric3.spi.model.instance.ValueSource;
import static org.fabric3.spi.model.instance.ValueSource.ValueSourceType.PROPERTY;
import static org.fabric3.spi.model.instance.ValueSource.ValueSourceType.REFERENCE;
import static org.fabric3.spi.model.instance.ValueSource.ValueSourceType.SERVICE;

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

    public void processConstructorSites(PojoComponentType type,
                                        InstanceFactoryDefinition providerDefinition) {
        Map<String, JavaMappedReference> references = type.getReferences();
        Map<String, JavaMappedProperty<?>> properties = type.getProperties();
        Map<String, JavaMappedService> services = type.getServices();

        // process constructor injectors
        ConstructorDefinition<?> ctorDef = type.getConstructorDefinition();
        for (String name : ctorDef.getInjectionNames()) {
            JavaMappedReference reference = references.get(name);
            if (reference != null) {
                ValueSource source = new ValueSource(REFERENCE, name);
                providerDefinition.addCdiSource(source);
                continue;
            }
            Property<?> property = properties.get(name);
            if (property != null) {
                ValueSource source = new ValueSource(PROPERTY, name);
                providerDefinition.addCdiSource(source);
                continue;
            }
            JavaMappedService service = services.get(name);
            if (service != null) {
                // SPEC The SCA spec does not specifically allow this yet -  submit an enhnacement request
                ValueSource source = new ValueSource(SERVICE, name);
                providerDefinition.addCdiSource(source);
                continue;
            }
            throw new AssertionError();
        }

    }

    public void processReferenceSites(LogicalComponent<? extends Implementation<PojoComponentType>> component,
                                      InstanceFactoryDefinition providerDefinition) {
        Implementation<PojoComponentType> implementation = component.getDefinition().getImplementation();
        PojoComponentType type = implementation.getComponentType();
        Map<String, JavaMappedReference> references = type.getReferences();
        for (Map.Entry<String, JavaMappedReference> entry : references.entrySet()) {
            JavaMappedReference reference = entry.getValue();
            MemberSite memberSite = reference.getMemberSite();
            if (memberSite == null) {
                // JFM this is dubious, the reference is mapped to a constructor so skip processing
                // ImplementationProcessorService does not set the member type to a ctor when creating the ref
                continue;
            } else if (!reference.isRequired()) {
                // if the reference is not required and is not configured, do not add an injection site
                // TODO: we should revisit the way this and ctors are handled above
                LogicalReference logicalReference = component.getReference(reference.getName());
                if (logicalReference == null
                        || (logicalReference.getBindings().isEmpty() && logicalReference.getTargetUris().isEmpty())) {
                    continue;
                }
            }
            ValueSource source = new ValueSource(REFERENCE, entry.getKey());

            InjectionSiteMapping mapping = new InjectionSiteMapping();
            mapping.setSource(source);
            mapping.setSite(memberSite);
            mapping.setMultiplicity(reference.getMultiplicity());

            providerDefinition.addInjectionSite(mapping);
        }

    }

    public void processConstructorArguments(ConstructorDefinition<?> ctorDef,
                                            InstanceFactoryDefinition providerDefinition) {
        for (String type : ctorDef.getParameterTypes()) {
            providerDefinition.addConstructorArgument(type);
        }
    }

    public void processProperties(PojoComponentDefinition physical,
                                  ComponentDefinition<? extends Implementation<PojoComponentType>> logical) {
        PojoComponentType type = logical.getImplementation().getComponentType();
        InstanceFactoryDefinition providerDefinition = physical.getInstanceFactoryProviderDefinition();
        Map<String, JavaMappedProperty<?>> properties = type.getProperties();
        Map<String, PropertyValue> propertyValues = logical.getPropertyValues();
        for (Map.Entry<String, JavaMappedProperty<?>> entry : properties.entrySet()) {
            String name = entry.getKey();
            JavaMappedProperty<?> property = entry.getValue();
            PropertyValue propertyValue = propertyValues.get(name);
            Document value;
            if (propertyValue != null) {
                value = propertyValue.getValue();
            } else {
                value = property.getDefaultValue();
            }

            if (value == null) {
                // nothing to inject
                continue;
            }

            MemberSite memberSite = property.getMemberSite();
            if (memberSite != null) {
                // set up the injection site
                ValueSource source = new ValueSource(PROPERTY, name);

                InjectionSiteMapping mapping = new InjectionSiteMapping();
                mapping.setSource(source);
                mapping.setSite(memberSite);
                providerDefinition.addInjectionSite(mapping);
            }

            physical.setPropertyValue(name, value);
        }
    }

    public void processResourceSites(LogicalComponent<? extends Implementation<PojoComponentType>> component, InstanceFactoryDefinition providerDefinition) {
        
        Implementation<PojoComponentType> implementation = component.getDefinition().getImplementation();
        PojoComponentType type = implementation.getComponentType();
        
        for (Map.Entry<String, JavaMappedResource<?>> entry : type.getResources().entrySet()) {
            
            JavaMappedResource<?> resource = entry.getValue();
            MemberSite memberSite = new MemberSite(resource.getMember());
            
            if (memberSite != null) {
                LogicalResource<?> logicalResource = component.getResource(resource.getName());
                if (logicalResource != null) {
                    ValueSource source = new ValueSource(REFERENCE, entry.getKey());

                    InjectionSiteMapping mapping = new InjectionSiteMapping();
                    mapping.setSource(source);
                    mapping.setSite(memberSite);

                    providerDefinition.addInjectionSite(mapping);
                }
            }
            
        }
        
    }
    
}
