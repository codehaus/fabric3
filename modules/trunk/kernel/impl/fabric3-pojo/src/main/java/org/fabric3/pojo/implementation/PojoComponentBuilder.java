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
package org.fabric3.pojo.implementation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.fabric3.pojo.injection.ListMultiplicityObjectFactory;
import org.fabric3.pojo.injection.MapMultiplicityObjectFactory;
import org.fabric3.pojo.injection.MultiplicityObjectFactory;
import org.fabric3.pojo.injection.SetMultiplicityObjectFactory;
import org.fabric3.scdl.InjectionSiteMapping;
import org.fabric3.pojo.instancefactory.InstanceFactoryBuilderRegistry;
import org.fabric3.pojo.instancefactory.InstanceFactoryDefinition;
import org.fabric3.scdl.InjectionSite;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.SingletonObjectFactory;
import org.fabric3.spi.builder.BuilderException;
import org.fabric3.spi.builder.component.ComponentBuilder;
import org.fabric3.spi.builder.component.ComponentBuilderRegistry;
import org.fabric3.spi.component.Component;
import org.fabric3.spi.component.InstanceFactoryProvider;
import org.fabric3.spi.component.ScopeRegistry;
import org.fabric3.scdl.ValueSource;
import org.fabric3.scdl.ValueSource.ValueSourceType;
import org.fabric3.spi.model.type.JavaClass;
import org.fabric3.spi.model.type.XSDSimpleType;
import org.fabric3.spi.services.classloading.ClassLoaderRegistry;
import org.fabric3.spi.transform.PullTransformer;
import org.fabric3.spi.transform.TransformerRegistry;
import org.fabric3.spi.transform.TransformContext;

/**
 * Base class for ComponentBuilders that build components based on POJOs.
 *
 * @version $Rev$ $Date$
 */
public abstract class PojoComponentBuilder<T, PCD extends PojoComponentDefinition, C extends Component>
        implements ComponentBuilder<PCD, C> {

    public static final ValueSource COMPONENT_CONTEXT_SOURCE = new ValueSource(ValueSource.ValueSourceType.CONTEXT, "ComponentContext");
    public static final ValueSource REQUEST_CONTEXT_SOURCE = new ValueSource(ValueSource.ValueSourceType.CONTEXT, "RequestContext");

    protected final ComponentBuilderRegistry builderRegistry;
    protected final ScopeRegistry scopeRegistry;
    protected final InstanceFactoryBuilderRegistry providerBuilders;
    protected final ClassLoaderRegistry classLoaderRegistry;
    protected final TransformerRegistry<PullTransformer<?,?>> transformerRegistry;

    private static final XSDSimpleType SOURCE_TYPE = new XSDSimpleType(Node.class, XSDSimpleType.STRING);
    private static final Map<Class<?>, Class<?>> OBJECT_TYPES;
    static {
        OBJECT_TYPES = new HashMap<Class<?>, Class<?>>();
        OBJECT_TYPES.put(Boolean.TYPE, Boolean.class);
        OBJECT_TYPES.put(Byte.TYPE, Byte.class);
        OBJECT_TYPES.put(Short.TYPE, Short.class);
        OBJECT_TYPES.put(Integer.TYPE, Integer.class);
        OBJECT_TYPES.put(Long.TYPE, Long.class);
        OBJECT_TYPES.put(Float.TYPE, Float.class);
        OBJECT_TYPES.put(Double.TYPE, Double.class);
    }

    protected PojoComponentBuilder(
            ComponentBuilderRegistry builderRegistry,
            ScopeRegistry scopeRegistry,
            InstanceFactoryBuilderRegistry providerBuilders,
            ClassLoaderRegistry classLoaderRegistry,
            TransformerRegistry<PullTransformer<?, ?>> transformerRegistry) {
        this.builderRegistry = builderRegistry;
        this.scopeRegistry = scopeRegistry;
        this.providerBuilders = providerBuilders;
        this.classLoaderRegistry = classLoaderRegistry;
        this.transformerRegistry = transformerRegistry;
    }

    protected Map<String, ObjectFactory<?>> createPropertyFactories(PCD definition,
                                                                    InstanceFactoryProvider<T> provider)
            throws BuilderException {
        Map<String, Document> propertyValues = definition.getPropertyValues();
        ConcurrentHashMap<String, ObjectFactory<?>> factories =
                new ConcurrentHashMap<String, ObjectFactory<?>>(propertyValues.size());

        ClassLoader cl = classLoaderRegistry.getClassLoader(definition.getClassLoaderId());
        TransformContext context = new TransformContext(null, cl, null, null);
        for (Map.Entry<String, Document> entry : propertyValues.entrySet()) {
            String name = entry.getKey();
            Document value = entry.getValue();
            Element element = value.getDocumentElement();
            ValueSource source = new ValueSource(ValueSource.ValueSourceType.PROPERTY, name);
            Class<?> memberType = provider.getMemberType(source);
            if (memberType.isPrimitive()) {
                memberType = OBJECT_TYPES.get(memberType);
            }
            ObjectFactory<?> objectFactory = createObjectFactory(name, memberType, element, context);
            provider.setObjectFactory(source, objectFactory);
            factories.put(name, objectFactory);
        }
        return factories;
    }

    protected <I> ObjectFactory<I> createObjectFactory(String name, Class<I> type, Element value, TransformContext context)
            throws BuilderException {
        JavaClass<I> targetType = new JavaClass<I>(type);
        PullTransformer<Node, I> transformer = getTransformer(SOURCE_TYPE, targetType);
        if (transformer == null) {
            throw new PropertyTransformException("No transformer for property of type " + type.getCanonicalName(), name, null);
        }
        try {
            I instance = type.cast(transformer.transform(value, context));
            return new SingletonObjectFactory<I>(instance);
        } catch (Exception e) {
            throw new PropertyTransformException("Unable to transform property value", name, e);
        }
    }

    @SuppressWarnings("unchecked")
    protected <I> PullTransformer<Node,I> getTransformer(XSDSimpleType source, JavaClass<I> target) {
        return (PullTransformer<Node,I>) transformerRegistry.getTransformer(source, target);
    }

    /**
     * Creates object factories for references of multiplicty greater than one.
     * 
     * @param providerDefinition Instance factory provider definition.
     * @return Map of reference names to multiplicty object factories.
     */
    protected Map<String, MultiplicityObjectFactory<?>> createMultiplicityReferenceFactories(InstanceFactoryDefinition providerDefinition) {

        Map<String, MultiplicityObjectFactory<?>> referenceFactories = new HashMap<String, MultiplicityObjectFactory<?>>();
        
        for (InjectionSiteMapping injectionSiteMapping : providerDefinition.getInjectionSites()) {
            
            if (injectionSiteMapping.getSource().getValueType() != ValueSource.ValueSourceType.REFERENCE) {
                continue;
            }
            
            InjectionSite injectionSite = injectionSiteMapping.getSite();
            String referenceType = injectionSite.getType();
            addMultiplicityFactory(referenceType, injectionSiteMapping.getSource(), referenceFactories);
            
        }
        
        List<String> ctrArguments = providerDefinition.getConstructorArguments();
        List<ValueSource> cdiSources = providerDefinition.getCdiSources();
        
        for (int i = 0;i < cdiSources.size();i++) {
            
            ValueSource cdiSource = cdiSources.get(i);
            
            if(cdiSource.getValueType() != ValueSourceType.REFERENCE) {
                continue;
            }
            
            String referenceType = ctrArguments.get(i);
            addMultiplicityFactory(referenceType, cdiSource, referenceFactories);
            
        }
        
        return referenceFactories;
        
    }
    
    /*
     * Adds the multiplicty reference factories.
     */
    private void addMultiplicityFactory(String referenceType, ValueSource valueSource, Map<String, MultiplicityObjectFactory<?>> referenceFactories) {

        if ("java.util.Map".equals(referenceType)) {
            referenceFactories.put(valueSource.getName(), new MapMultiplicityObjectFactory());
        } else if ("java.util.Set".equals(referenceType)) {
            referenceFactories.put(valueSource.getName(), new SetMultiplicityObjectFactory());
        } else if ("java.util.List".equals(referenceType)) {
            referenceFactories.put(valueSource.getName(), new ListMultiplicityObjectFactory());
        } else if ("java.util.Collection".equals(referenceType)) {
            referenceFactories.put(valueSource.getName(), new ListMultiplicityObjectFactory());
        }
    }
}
