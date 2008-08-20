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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.fabric3.pojo.instancefactory.InstanceFactoryBuilderRegistry;
import org.fabric3.scdl.DataType;
import org.fabric3.scdl.InjectableAttribute;
import org.fabric3.scdl.InjectableAttributeType;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.SingletonObjectFactory;
import org.fabric3.spi.builder.BuilderException;
import org.fabric3.spi.builder.component.ComponentBuilder;
import org.fabric3.spi.builder.component.ComponentBuilderRegistry;
import org.fabric3.spi.component.Component;
import org.fabric3.spi.component.InstanceFactoryProvider;
import org.fabric3.spi.component.ScopeRegistry;
import org.fabric3.spi.model.type.JavaClass;
import org.fabric3.spi.model.type.JavaParameterizedType;
import org.fabric3.spi.model.type.XSDSimpleType;
import org.fabric3.spi.services.classloading.ClassLoaderRegistry;
import org.fabric3.transform.PullTransformer;
import org.fabric3.transform.TransformContext;
import org.fabric3.transform.TransformerRegistry;

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
    protected final TransformerRegistry<PullTransformer<?, ?>> transformerRegistry;

    private static final XSDSimpleType SOURCE_TYPE = new XSDSimpleType(Node.class, XSDSimpleType.STRING);
    private static final Map<Type, Class<?>> OBJECT_TYPES;

    static {
        OBJECT_TYPES = new HashMap<Type, Class<?>>();
        OBJECT_TYPES.put(Boolean.TYPE, Boolean.class);
        OBJECT_TYPES.put(Byte.TYPE, Byte.class);
        OBJECT_TYPES.put(Short.TYPE, Short.class);
        OBJECT_TYPES.put(Integer.TYPE, Integer.class);
        OBJECT_TYPES.put(Long.TYPE, Long.class);
        OBJECT_TYPES.put(Float.TYPE, Float.class);
        OBJECT_TYPES.put(Double.TYPE, Double.class);
    }

    protected PojoComponentBuilder(ComponentBuilderRegistry builderRegistry,
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

    protected void createPropertyFactories(PCD definition, InstanceFactoryProvider<T> provider) throws BuilderException {
        Map<String, Document> propertyValues = definition.getPropertyValues();

        ClassLoader cl = classLoaderRegistry.getClassLoader(definition.getClassLoaderId());
        TransformContext context = new TransformContext(null, cl, null, null);
        for (Map.Entry<String, Document> entry : propertyValues.entrySet()) {
            String name = entry.getKey();
            Document value = entry.getValue();
            Element element = value.getDocumentElement();
            InjectableAttribute source = new InjectableAttribute(InjectableAttributeType.PROPERTY, name);

            Type memberType = provider.getGenericType(source);
            if (memberType instanceof Class<?> ||
                    (memberType instanceof ParameterizedType && ((ParameterizedType) memberType).getRawType().equals(Class.class))) {
                memberType = provider.getMemberType(source);
                if (((Class<?>) memberType).isPrimitive()) {
                    memberType = OBJECT_TYPES.get(memberType);
                }
            }

            ObjectFactory<?> objectFactory = createObjectFactory(name, memberType, element, context);
            provider.setObjectFactory(source, objectFactory);
        }
    }

    @SuppressWarnings("unchecked")
    private ObjectFactory<?> createObjectFactory(String name, Type type, Element value, TransformContext context) throws BuilderException {

        DataType<?> targetType = null;

        if (type instanceof Class<?>) {
            targetType = new JavaClass((Class<?>) type);
        } else if (type instanceof ParameterizedType) {
            targetType = new JavaParameterizedType((ParameterizedType) type);
        }

        PullTransformer<Node, ?> transformer = (PullTransformer<Node, ?>) transformerRegistry.getTransformer(SOURCE_TYPE, targetType);
        if (transformer == null) {
            throw new PropertyTransformException("No transformer for property of type: " + type, name, null);
        }

        try {
            Object instance = transformer.transform(value, context);
            return new SingletonObjectFactory(instance);
        } catch (Exception e) {
            throw new PropertyTransformException("Unable to transform property value: " + name, name, e);
        }

    }

}
