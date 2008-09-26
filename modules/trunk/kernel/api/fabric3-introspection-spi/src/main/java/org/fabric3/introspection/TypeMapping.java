/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 * --- Original Apache License ---
 *
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
package org.fabric3.introspection;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.HashMap;
import java.util.Map;

/**
 * A mapping from formal types to actual types.
 *
 * @version $Rev$ $Date$
 */
public class TypeMapping {
    private final Map<? super Type, Type> mappings = new HashMap<Type, Type>();

    /**
     * Add a mapping from a TypeVariable to an actual type
     *
     * @param typeVariable the formal type variable
     * @param type         the actual type it maps to
     */
    public void addMapping(TypeVariable<?> typeVariable, Type type) {
        mappings.put(typeVariable, type);
    }

    /**
     * Return the actual type of the supplied formal type.
     *
     * @param type the formal type parameter
     * @return the actual type; may be a TypeVariable if the type is not bound
     */
    public Type getActualType(Type type) {
        while (true) {
            Type actual = mappings.get(type);
            if (actual == null) {
                return type;
            } else {
                type = actual;
            }
        }
    }

    /**
     * Return the raw type of the supplied formal type.
     *
     * @param type the formal type parameter
     * @return the actual class for that parameter
     */
    public Class<?> getRawType(Type type) {
        Type actualType = getActualType(type);
        if (actualType instanceof Class<?>) {
            return (Class<?>) actualType;
        } else if (actualType instanceof TypeVariable) {
            TypeVariable typeVariable = (TypeVariable) actualType;
            return getRawType(typeVariable.getBounds()[0]);
        } else if (actualType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) actualType;
            return (Class<?>) parameterizedType.getRawType();
        } else if (actualType instanceof GenericArrayType) {
            GenericArrayType arrayType = (GenericArrayType) actualType;
            Class<?> componentType = getRawType(arrayType.getGenericComponentType());
            return Array.newInstance(componentType, 0).getClass();
        } else if (actualType instanceof WildcardType) {
            WildcardType wildcardType = (WildcardType) actualType;
            Type[] bounds = wildcardType.getUpperBounds();
            return getRawType(bounds[0]);
        } else {
            throw new AssertionError();
        }
    }
}