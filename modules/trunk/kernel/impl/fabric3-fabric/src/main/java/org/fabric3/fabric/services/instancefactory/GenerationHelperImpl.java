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
import org.fabric3.scdl.InjectionSiteMapping;
import org.fabric3.pojo.instancefactory.InstanceFactoryDefinition;
import org.fabric3.pojo.instancefactory.InstanceFactoryGenerationHelper;
import org.fabric3.scdl.Signature;
import org.fabric3.pojo.scdl.ConstructorDefinition;
import org.fabric3.pojo.scdl.JavaMappedProperty;
import org.fabric3.pojo.scdl.JavaMappedReference;
import org.fabric3.pojo.scdl.JavaMappedResource;
import org.fabric3.pojo.scdl.JavaMappedService;
import org.fabric3.scdl.InjectionSite;
import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.scdl.ComponentDefinition;
import org.fabric3.scdl.Implementation;
import org.fabric3.scdl.Property;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalResource;
import org.fabric3.scdl.ValueSource;
import org.fabric3.scdl.FieldInjectionSite;
import org.fabric3.scdl.MethodInjectionSite;
import org.fabric3.scdl.ConstructorInjectionSite;
import static org.fabric3.scdl.ValueSource.ValueSourceType.CONTEXT;
import static org.fabric3.scdl.ValueSource.ValueSourceType.PROPERTY;
import static org.fabric3.scdl.ValueSource.ValueSourceType.REFERENCE;
import static org.fabric3.scdl.ValueSource.ValueSourceType.RESOURCE;
import static org.fabric3.scdl.ValueSource.ValueSourceType.CALLBACK;

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
/*
        processConstructorSites(type, providerDefinition);
        processPropertySites(component, providerDefinition);
        processReferenceSites(component, providerDefinition);
        processCallbackSites(component, providerDefinition);
        processResourceSites(component, providerDefinition);
*/
        processContextSites(component, providerDefinition);
    }

    private void processConstructorSites(PojoComponentType type, InstanceFactoryDefinition providerDefinition) {
        Map<String, JavaMappedReference> references = type.getReferences();
        Map<String, JavaMappedProperty<?>> properties = type.getProperties();
        Map<String, JavaMappedService> services = type.getServices();

        // process constructor injectors
        ConstructorDefinition ctorDef = type.getConstructorDefinition();
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
                ValueSource source = new ValueSource(CALLBACK, name);
                providerDefinition.addCdiSource(source);
                continue;
            }
            throw new AssertionError();
        }

    }

    private void processReferenceSites(LogicalComponent<? extends Implementation<PojoComponentType>> component,
                                      InstanceFactoryDefinition providerDefinition) {
        Implementation<PojoComponentType> implementation = component.getDefinition().getImplementation();
        PojoComponentType type = implementation.getComponentType();
        Map<String, JavaMappedReference> references = type.getReferences();
        for (Map.Entry<String, JavaMappedReference> entry : references.entrySet()) {
            ValueSource source = new ValueSource(REFERENCE, entry.getKey());
            JavaMappedReference reference = entry.getValue();
            InjectionSite injectionSite = type.getInjectionSite(source);
            if (injectionSite == null) {
                // JFM this is dubious, the reference is mapped to a constructor so skip processing
                // ImplementationProcessorService does not set the member type to a ctor when creating the ref
                continue;
            } else if (!reference.isRequired()) {
                // if the reference is not required and is not configured, do not add an injection site
                // TODO: we should revisit the way this and ctors are handled above
                LogicalReference logicalReference = component.getReference(reference.getName());
                if (logicalReference == null || (logicalReference.getBindings().isEmpty() && logicalReference.getTargetUris().isEmpty())) {
                    continue;
                }
            }

            InjectionSiteMapping mapping = new InjectionSiteMapping();
            mapping.setSource(source);
            mapping.setSite(injectionSite);

            providerDefinition.addInjectionSite(mapping);
        }

    }

    private void processPropertySites(LogicalComponent<? extends Implementation<PojoComponentType>> component,
                                     InstanceFactoryDefinition providerDefinition) {
        Implementation<PojoComponentType> implementation = component.getDefinition().getImplementation();
        PojoComponentType type = implementation.getComponentType();
        for (JavaMappedProperty<?> property : type.getProperties().values()) {
            String name = property.getName();
            InjectionSite injectionSite = property.getMemberSite();
            Document value = component.getPropertyValue(name);
            // add mapping for property only if it is mapped and has an explicit value defined
            if (injectionSite != null && value != null) {
                addMapping(providerDefinition, new ValueSource(PROPERTY, name), injectionSite);
            }
        }
    }


    private void processCallbackSites(LogicalComponent<? extends Implementation<PojoComponentType>> component,
                                      InstanceFactoryDefinition providerDefinition) {

        Implementation<PojoComponentType> implementation = component.getDefinition().getImplementation();
        PojoComponentType type = implementation.getComponentType();

        for (InjectionSite site : type.getCallbackSites()) {
            // JFM move getName to InjectionSite  
            String name;
            if (site instanceof FieldInjectionSite) {
                name = ((FieldInjectionSite) site).getName();
            } else if (site instanceof MethodInjectionSite) {
                name = ((MethodInjectionSite) site).getSignature().getName();
            } else if (site instanceof ConstructorInjectionSite) {
                name = ((ConstructorInjectionSite) site).getSignature().getName();
            } else {
                throw new UnsupportedClassVersionError("Unknown InjectionSite type [" + site.getClass() + "]");
            }
            ValueSource source = new ValueSource(CALLBACK, name);
            addMapping(providerDefinition, source, site);
        }
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

    private void processResourceSites(LogicalComponent<? extends Implementation<PojoComponentType>> component,
                                     InstanceFactoryDefinition providerDefinition) {

        Implementation<PojoComponentType> implementation = component.getDefinition().getImplementation();
        PojoComponentType type = implementation.getComponentType();

        for (Map.Entry<String, JavaMappedResource> entry : type.getResources().entrySet()) {
            JavaMappedResource resource = entry.getValue();
            LogicalResource<?> logicalResource = component.getResource(resource.getName());
            if (logicalResource != null) {
                addMapping(providerDefinition, new ValueSource(RESOURCE, entry.getKey()), resource.getMemberSite());
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
