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
import java.net.URI;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.fabric3.model.type.java.InjectableAttribute;
import org.fabric3.model.type.contract.DataType;
import org.fabric3.pojo.component.PojoComponent;
import org.fabric3.pojo.provision.PojoSourceDefinition;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.model.physical.PhysicalTargetDefinition;
import org.fabric3.spi.model.type.java.JavaClass;
import org.fabric3.spi.model.type.xsd.XSDSimpleType;
import org.fabric3.spi.transform.PullTransformer;
import org.fabric3.spi.transform.PullTransformerRegistry;
import org.fabric3.spi.transform.TransformContext;
import org.fabric3.spi.transform.TransformationException;

/**
 * @version $Rev$ $Date$
 */
public abstract class PojoSourceWireAttacher {

    private static final XSDSimpleType SOURCE_TYPE = new XSDSimpleType(Node.class, XSDSimpleType.STRING);

    protected PullTransformerRegistry transformerRegistry;
    protected ClassLoaderRegistry classLoaderRegistry;

    protected PojoSourceWireAttacher(PullTransformerRegistry transformerRegistry, ClassLoaderRegistry loaderRegistry) {
        this.transformerRegistry = transformerRegistry;
        this.classLoaderRegistry = loaderRegistry;
   }

    @SuppressWarnings("unchecked")
    protected Object getKey(PojoSourceDefinition sourceDefinition,
                            PojoComponent<?> source,
                            PhysicalTargetDefinition targetDefinition,
                            InjectableAttribute referenceSource) throws PropertyTransformException {

        if (!Map.class.isAssignableFrom(source.getMemberType(referenceSource))) {
            return null;
        }

        Document keyDocument = sourceDefinition.getKey();
        if (keyDocument == null) {
            keyDocument = targetDefinition.getKey();
        }


        if (keyDocument != null) {

            Element element = keyDocument.getDocumentElement();
            URI targetId = targetDefinition.getClassLoaderId();
            ClassLoader targetClassLoader = null;
            if (targetId != null) {
                targetClassLoader = classLoaderRegistry.getClassLoader(targetId);
            }

            Type formalType;
            Type type = source.getGenericMemberType(referenceSource);

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

            if (!(formalType instanceof Class)) {
                throw new PropertyTransformException("Unsupported key type:: " + formalType);
            }
            DataType<?> targetType = new JavaClass((Class) formalType);
            return createKey(targetType, element, targetClassLoader);
        }

        return null;

    }


    @SuppressWarnings("unchecked")
    private Object createKey(DataType<?> targetType, Element value, ClassLoader classLoader) throws PropertyTransformException {

        PullTransformer<Node, ?> transformer = (PullTransformer<Node, ?>) transformerRegistry.getTransformer(SOURCE_TYPE, targetType);
        if (transformer == null) {
            throw new PropertyTransformException("No transformer for : " + targetType);
        }
        try {
            TransformContext context = new TransformContext(SOURCE_TYPE, targetType, classLoader);
            return transformer.transform(value, context);
        } catch (TransformationException e) {
            throw new PropertyTransformException("Error transformatng property", e);
        }
    }

}
