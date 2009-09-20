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

import java.lang.reflect.Type;
import java.util.List;

import org.osoa.sca.annotations.Reference;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.fabric3.model.type.java.InjectableAttribute;
import org.fabric3.model.type.java.InjectableAttributeType;
import org.fabric3.model.type.service.DataType;
import org.fabric3.pojo.instancefactory.InstanceFactoryProvider;
import org.fabric3.pojo.provision.PojoComponentDefinition;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.SingletonObjectFactory;
import org.fabric3.spi.builder.BuilderException;
import org.fabric3.spi.builder.component.ComponentBuilder;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.component.Component;
import org.fabric3.spi.expression.ExpressionExpander;
import org.fabric3.spi.expression.ExpressionExpansionException;
import org.fabric3.spi.introspection.IntrospectionHelper;
import org.fabric3.spi.introspection.TypeMapping;
import org.fabric3.spi.model.physical.PhysicalPropertyDefinition;
import org.fabric3.spi.model.type.JavaClass;
import org.fabric3.spi.model.type.JavaGenericType;
import org.fabric3.spi.model.type.JavaTypeInfo;
import org.fabric3.spi.model.type.XSDSimpleType;
import org.fabric3.spi.transform.PullTransformer;
import org.fabric3.spi.transform.PullTransformerRegistry;
import org.fabric3.spi.transform.TransformContext;
import org.fabric3.spi.transform.TransformationException;
import org.fabric3.spi.util.ParamTypes;

/**
 * Base class for ComponentBuilders that build components based on POJOs.
 *
 * @version $Rev$ $Date$
 */
public abstract class PojoComponentBuilder<T, PCD extends PojoComponentDefinition, C extends Component> implements ComponentBuilder<PCD, C> {
    private static final XSDSimpleType SOURCE_TYPE = new XSDSimpleType(Node.class, XSDSimpleType.STRING);

    protected ClassLoaderRegistry classLoaderRegistry;
    protected PullTransformerRegistry transformerRegistry;
    protected ExpressionExpander expander;

    protected IntrospectionHelper helper;

    protected PojoComponentBuilder(ClassLoaderRegistry classLoaderRegistry, PullTransformerRegistry transformerRegistry, IntrospectionHelper helper) {
        this.classLoaderRegistry = classLoaderRegistry;
        this.transformerRegistry = transformerRegistry;
        this.helper = helper;
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
        List<PhysicalPropertyDefinition> propertyDefinitions = definition.getPropertyDefinitions();

        TypeMapping typeMapping = new TypeMapping();
        helper.resolveTypeParameters(provider.getImplementationClass(), typeMapping);

        for (PhysicalPropertyDefinition propertyDefinition : propertyDefinitions) {
            String name = propertyDefinition.getName();
            Document value = propertyDefinition.getValue();
            Element element = value.getDocumentElement();
            InjectableAttribute source = new InjectableAttribute(InjectableAttributeType.PROPERTY, name);

            Type type = provider.getGenericType(source);
            DataType<?> dataType = getDataType(type, typeMapping);

            ClassLoader classLoader = classLoaderRegistry.getClassLoader(definition.getClassLoaderId());
            ObjectFactory<?> objectFactory = createObjectFactory(name, dataType, element, classLoader);
            provider.setObjectFactory(source, objectFactory);
        }
    }

    private DataType<?> getDataType(Type type, TypeMapping typeMapping) {
        if (type instanceof Class) {
            // non-generic type
            Class<?> nonGenericType = (Class<?>) type;
            if (nonGenericType.isPrimitive()) {
                // convert primitive representation to its object equivalent
                nonGenericType = ParamTypes.PRIMITIVE_TO_OBJECT.get(nonGenericType);
            }
            return new JavaClass(nonGenericType);
        } else {
            // a generic
            JavaTypeInfo info = helper.createTypeInfo(type, typeMapping);
            return new JavaGenericType(info);

        }
    }

    @SuppressWarnings("unchecked")
    private ObjectFactory<?> createObjectFactory(String name, DataType<?> dataType, Element value, ClassLoader classLoader) throws BuilderException {

        PullTransformer<Node, ?> transformer = (PullTransformer<Node, ?>) transformerRegistry.getTransformer(SOURCE_TYPE, dataType);
        if (transformer == null) {
            throw new PropertyTransformException("No transformer for property " + name + " of type: " + dataType);
        }

        try {
            TransformContext context = new TransformContext(SOURCE_TYPE, dataType, classLoader);
            Object instance = transformer.transform(value, context);
            if (instance instanceof String && expander != null) {
                // if the property value is a string, expand it if it contains expressions
                instance = expander.expand((String) instance);
            }
            return new SingletonObjectFactory(instance);
        } catch (TransformationException e) {
            throw new PropertyTransformException("Unable to transform property value: " + name, e);
        } catch (ExpressionExpansionException e) {
            throw new PropertyTransformException("Unable to expand property value: " + name, e);
        }

    }

}
