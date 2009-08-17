/*
 * Fabric3
 * Copyright (c) 2009 Metaform Systems
 *
 * Fabric3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version, with the
 * following exception:
 *
 * Linking this software statically or dynamically with other
 * modules is making a combined work based on this software.
 * Thus, the terms and conditions of the GNU General Public
 * License cover the whole combination.
 *
 * As a special exception, the copyright holders of this software
 * give you permission to link this software with independent
 * modules to produce an executable, regardless of the license
 * terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided
 * that you also meet, for each linked independent module, the
 * terms and conditions of the license of that module. An
 * independent module is a module which is not derived from or
 * based on this software. If you modify this software, you may
 * extend this exception to your version of the software, but
 * you are not obligated to do so. If you do not wish to do so,
 * delete this exception statement from your version.
 *
 * Fabric3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the
 * GNU General Public License along with Fabric3.
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.pojo.builder;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import org.osoa.sca.annotations.Reference;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.fabric3.model.type.java.InjectableAttribute;
import org.fabric3.model.type.java.InjectableAttributeType;
import org.fabric3.model.type.service.DataType;
import org.fabric3.pojo.instancefactory.InstanceFactoryBuilderRegistry;
import org.fabric3.pojo.instancefactory.InstanceFactoryProvider;
import org.fabric3.pojo.provision.PojoComponentDefinition;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.SingletonObjectFactory;
import org.fabric3.spi.expression.ExpressionExpander;
import org.fabric3.spi.builder.BuilderException;
import org.fabric3.spi.builder.component.ComponentBuilder;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.component.Component;
import org.fabric3.spi.component.ScopeRegistry;
import org.fabric3.spi.model.type.JavaClass;
import org.fabric3.spi.model.type.JavaParameterizedType;
import org.fabric3.spi.model.type.XSDSimpleType;
import org.fabric3.spi.transform.PullTransformer;
import org.fabric3.spi.transform.TransformContext;
import org.fabric3.spi.transform.TransformerRegistry;

/**
 * Base class for ComponentBuilders that build components based on POJOs.
 *
 * @version $Rev$ $Date$
 */
public abstract class PojoComponentBuilder<T, PCD extends PojoComponentDefinition, C extends Component> implements ComponentBuilder<PCD, C> {

    protected final ScopeRegistry scopeRegistry;
    protected final InstanceFactoryBuilderRegistry providerBuilders;
    protected final ClassLoaderRegistry classLoaderRegistry;
    protected final TransformerRegistry<PullTransformer<?, ?>> transformerRegistry;
    protected ExpressionExpander expander;

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

    protected PojoComponentBuilder(ScopeRegistry scopeRegistry,
                                   InstanceFactoryBuilderRegistry providerBuilders,
                                   ClassLoaderRegistry classLoaderRegistry,
                                   TransformerRegistry<PullTransformer<?, ?>> transformerRegistry) {
        this.scopeRegistry = scopeRegistry;
        this.providerBuilders = providerBuilders;
        this.classLoaderRegistry = classLoaderRegistry;
        this.transformerRegistry = transformerRegistry;
    }

    /**
     * Optional ExpressionExpander for substituting values for properties containing expressions of the form '${..}'. Values may be sourced from a
     * variety of places, including a file or system property.
     *
     * @param expander the injected expander
     */
    @Reference(required = false)
    public void setExpander(ExpressionExpander expander) {
        this.expander = expander;
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
            if (instance instanceof String && expander != null) {
                // if the property value is a string, expand it if it contains expressions
                instance = expander.expand((String) instance);
            }
            return new SingletonObjectFactory(instance);
        } catch (Exception e) {
            throw new PropertyTransformException("Unable to transform property value: " + name, name, e);
        }

    }

}
