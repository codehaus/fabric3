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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.fabric3.pojo.instancefactory.InstanceFactoryBuilderRegistry;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.SingletonObjectFactory;
import org.fabric3.spi.builder.BuilderException;
import org.fabric3.spi.builder.component.ComponentBuilder;
import org.fabric3.spi.builder.component.ComponentBuilderRegistry;
import org.fabric3.spi.component.Component;
import org.fabric3.spi.component.InstanceFactoryProvider;
import org.fabric3.spi.component.ScopeRegistry;
import org.fabric3.spi.model.instance.ValueSource;
import org.fabric3.spi.model.type.JavaClass;
import org.fabric3.spi.model.type.XSDSimpleType;
import org.fabric3.spi.services.classloading.ClassLoaderRegistry;
import org.fabric3.spi.transform.PullTransformer;
import org.fabric3.spi.transform.TransformerRegistry;

/**
 * Base class for ComponentBuilders that build components based on POJOs.
 *
 * @version $Rev$ $Date$
 */
public abstract class PojoComponentBuilder<T, PCD extends PojoComponentDefinition, C extends Component>
        implements ComponentBuilder<PCD, C> {

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

        for (Map.Entry<String, Document> entry : propertyValues.entrySet()) {
            String name = entry.getKey();
            Document value = entry.getValue();
            Element element = value.getDocumentElement();
            ValueSource source = new ValueSource(ValueSource.ValueSourceType.PROPERTY, name);
            Class<?> memberType = provider.getMemberType(source);
            ObjectFactory<?> objectFactory;
            if (Class.class.equals(memberType)) {
                String className = element.getTextContent();
                try {
                    Class<?> clazz = classLoaderRegistry.loadClass(definition.getClassLoaderId(), className);
                    objectFactory = new SingletonObjectFactory<Class<?>>(clazz);
                } catch (ClassNotFoundException e) {
                    throw new PropertyTransformException(e.getMessage(), className, e);
                }
            } else {
                if (memberType.isPrimitive()) {
                    memberType = OBJECT_TYPES.get(memberType);
                }
                objectFactory = createObjectFactory(name, memberType, element);
            }
            provider.setObjectFactory(source, objectFactory);
            factories.put(name, objectFactory);
        }
        return factories;
    }

    protected <T> ObjectFactory<T> createObjectFactory(String name, Class<T> type, Element value)
            throws BuilderException {
        JavaClass<T> targetType = new JavaClass<T>(type);
        PullTransformer<Node, T> transformer = getTransformer(SOURCE_TYPE, targetType);
        try {
            T instance = type.cast(transformer.transform(value));
            return new SingletonObjectFactory<T>(instance);
        } catch (Exception e) {
            throw new PropertyTransformException("Unable to transform property value", name, e);
        }
    }

    @SuppressWarnings("unchecked")
    protected <T> PullTransformer<Node,T> getTransformer(XSDSimpleType source, JavaClass<T> target) {
        return (PullTransformer<Node,T>) transformerRegistry.getTransformer(source, target);
    }
}
