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
package org.fabric3.pojo.builder;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.fabric3.pojo.component.PojoComponent;
import org.fabric3.pojo.provision.PojoWireSourceDefinition;
import org.fabric3.scdl.DataType;
import org.fabric3.scdl.InjectableAttribute;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.model.type.JavaClass;
import org.fabric3.spi.model.type.JavaParameterizedType;
import org.fabric3.spi.model.type.XSDSimpleType;
import org.fabric3.spi.services.classloading.ClassLoaderRegistry;
import org.fabric3.transform.PullTransformer;
import org.fabric3.transform.TransformContext;
import org.fabric3.transform.TransformationException;
import org.fabric3.transform.TransformerRegistry;

/**
 * @version $Revision$ $Date$
 */
public abstract class PojoSourceWireAttacher {

    private static final XSDSimpleType SOURCE_TYPE = new XSDSimpleType(Node.class, XSDSimpleType.STRING);

    private TransformerRegistry<PullTransformer<?, ?>> transformerRegistry;
    private ClassLoaderRegistry classLoaderRegistry;

    protected PojoSourceWireAttacher(TransformerRegistry<PullTransformer<?, ?>> transformerRegistry, ClassLoaderRegistry classLoaderRegistry) {
        this.transformerRegistry = transformerRegistry;
        this.classLoaderRegistry = classLoaderRegistry;
    }

    @SuppressWarnings("unchecked")
    protected Object getKey(PojoWireSourceDefinition sourceDefinition,
                            PojoComponent<?> source,
                            PhysicalWireTargetDefinition targetDefinition,
                            InjectableAttribute referenceSource) throws PropertyTransformException {

        if (!Map.class.isAssignableFrom(source.getMemberType(referenceSource))) {
            return null;
        }

        Document keyDocument = sourceDefinition.getKey();


        if (keyDocument != null) {

            Element element = keyDocument.getDocumentElement();

            Type formalType;
            Type type = source.getGerenricMemberType(referenceSource);

            if (type instanceof ParameterizedType) {
                ParameterizedType genericType = (ParameterizedType) type;
                formalType = genericType.getActualTypeArguments()[0];
                if (formalType instanceof ParameterizedType && ((ParameterizedType) formalType).getRawType().equals(Class.class)) {
                    formalType = ((ParameterizedType) formalType).getRawType();
                } else if (formalType instanceof Class<?> && Enum.class.isAssignableFrom((Class<?>) formalType)) {
                    Class<Enum> enumClass = (Class<Enum>) formalType;
                    return Enum.valueOf(enumClass, element.getTextContent());
                }
            } else {
                formalType = String.class;
            }

            URI sourceId = sourceDefinition.getClassLoaderId();
            URI targetId = targetDefinition.getClassLoaderId();
            ClassLoader sourceClassLoader = classLoaderRegistry.getClassLoader(sourceId);
            ClassLoader targetClassLoader = classLoaderRegistry.getClassLoader(targetId);

            TransformContext context = new TransformContext(sourceClassLoader, targetClassLoader, null, null);
            return createKey(formalType, element, context);
        }

        return null;

    }

    @SuppressWarnings("unchecked")
    private Object createKey(Type type, Element value, TransformContext context) throws PropertyTransformException {

        DataType<?> targetType;
        if (type instanceof Class<?>) {
            targetType = new JavaClass((Class<?>) type);
        } else {
            targetType = new JavaParameterizedType((ParameterizedType) type);
        }
        PullTransformer<Node, ?> transformer = (PullTransformer<Node, ?>) transformerRegistry.getTransformer(SOURCE_TYPE, targetType);
        if (transformer == null) {
            throw new PropertyTransformException("No transformer for : " + type);
        }
        try {
            return transformer.transform(value, context);
        } catch (TransformationException e) {
            throw new PropertyTransformException("Error transformatng property", e);
        }
    }

}
