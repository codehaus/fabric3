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
package org.fabric3.introspection.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.TypeVariable;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Map;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Queue;
import java.util.SortedSet;

import org.osoa.sca.annotations.Remotable;
import org.osoa.sca.annotations.Service;
import org.osoa.sca.ComponentContext;
import org.osoa.sca.RequestContext;

import org.fabric3.introspection.IntrospectionException;
import org.fabric3.introspection.IntrospectionHelper;
import org.fabric3.scdl.InjectableAttributeType;

/**
 * @version $Rev$ $Date$
 */
public class DefaultIntrospectionHelper implements IntrospectionHelper {
    // the Collection classes we understand and which all have a single type parameter
    private static final Set<Class<?>> COLLECTIONS;
    static {
        COLLECTIONS = new HashSet<Class<?>>();
        COLLECTIONS.add(Collection.class);
        COLLECTIONS.add(List.class);
        COLLECTIONS.add(Queue.class);
        COLLECTIONS.add(Set.class);
        COLLECTIONS.add(SortedSet.class); }

    public String getSiteName(Field field, String override) throws IntrospectionException {
        if (override != null && override.length() != 0) {
            return override;
        }
        return field.getName();
    }

    public String getSiteName(Method setter, String override) throws IntrospectionException {
        if (override != null && override.length() != 0) {
            return override;
        }

        String name = setter.getName();
        if (name.length() > 3 && name.startsWith("set")) {
            return Character.toLowerCase(name.charAt(3)) + name.substring(4);
        } else {
            return name;
        }
    }

    public String getSiteName(Constructor<?> constructor, int index, String override) throws IntrospectionException {
        if (override != null && override.length() != 0) {
            return override;
        }

        org.osoa.sca.annotations.Constructor annotation = constructor.getAnnotation(org.osoa.sca.annotations.Constructor.class);
        if (annotation != null) {
            String[] names = annotation.value();
            if (names.length != 1 || names[0].length() != 0) {
                return names[index];
            }
        }
        return constructor.getDeclaringClass().getSimpleName() + "[" + index + ']';
    }

    public Type getGenericType(Method setter) throws IntrospectionException {
        return getGenericType(setter, 0);
    }

    public Type getGenericType(Method method, int index) throws IntrospectionException {
        return method.getGenericParameterTypes()[index];
    }

    public Type getGenericType(Constructor<?> constructor, int index) throws IntrospectionException {
        return constructor.getGenericParameterTypes()[index];
    }

    public Class<?> getType(Method setter) throws IntrospectionException {
        return getType(setter, 0);
    }

    public Class<?> getType(Method method, int index) throws IntrospectionException {
        return method.getParameterTypes()[index];
    }

    public Class<?> getType(Constructor<?> constructor, int index) throws IntrospectionException {
        return constructor.getParameterTypes()[index];
    }

    public Type getBaseType(Type type) {
        if (type instanceof Class) {
            Class<?> clazz = (Class<?>) type;
            if (clazz.isArray()) {
                return clazz.getComponentType();
            } else if (COLLECTIONS.contains(clazz) || Map.class.equals(clazz)) {
                return Object.class;
            } else {
                return clazz;
            }
        } else if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Class<?> clazz = (Class<?>) parameterizedType.getRawType();
            Type[] typeArguments = parameterizedType.getActualTypeArguments();
            if (COLLECTIONS.contains(clazz)) {
                return getRawType(typeArguments[0]);
            } else if (Map.class.equals(clazz)) {
                return getRawType(typeArguments[1]);
            } else {
                return clazz;
            }

        } else if (type instanceof GenericArrayType) {
            GenericArrayType arrayType = (GenericArrayType) type;
            return getRawType(arrayType.getGenericComponentType());
        } else {
            throw new AssertionError("Unknown Type: " + type);
        }
    }

    public Class<?> getRawType(Type type) {
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            return getRawType(parameterizedType.getRawType());
        } else if (type instanceof TypeVariable) {
            TypeVariable typeVariable = (TypeVariable) type;
            Type[] bounds = typeVariable.getBounds();
            return (Class<?>) bounds[0];
        } else {
            return (Class<?>) type;
        }
    }

    public boolean isManyValued(Type type) {
        return type instanceof GenericArrayType || isManyValued(getRawType(type));
    }

    public boolean isManyValued(Class<?> clazz) {
        return clazz.isArray() || COLLECTIONS.contains(clazz) || Map.class.equals(clazz);
    }

    public InjectableAttributeType inferType(Type type) {
        Type baseType = getBaseType(type);
        Class<?> rawType = getRawType(baseType);

        // if it's not an interface, it must be a property
        if (!rawType.isInterface()) {
            return InjectableAttributeType.PROPERTY;
        }

        // it it's a context interfaces, it must be a context
        if (ComponentContext.class.isAssignableFrom(rawType) || RequestContext.class.isAssignableFrom(rawType)) {
            return InjectableAttributeType.CONTEXT;
        }

        // if it's Remotable or a local Service, it must be a reference
        if (isAnnotationPresent(rawType, Remotable.class) || isAnnotationPresent(rawType, Service.class)) {
            return InjectableAttributeType.REFERENCE;
        }

        // otherwise it's a property
        return InjectableAttributeType.PROPERTY;
    }

    public boolean isAnnotationPresent(Class<?> type, Class<? extends Annotation> annotationType) {
        if (type.isAnnotationPresent(annotationType)) {
            return true;
        }
        Class<?>[] interfaces = type.getInterfaces();
        for (Class<?> superInterface : interfaces) {
            if (isAnnotationPresent(superInterface, annotationType)) {
                return true;
            }
        }
        return false;
    }

    public Set<Class<?>> getImplementedInterfaces(Class<?> type) {
        Set<Class<?>> interfaces = new HashSet<Class<?>>();
        while (type != null) {
            nextInterface: for (Class<?> current : (Class<?>[]) type.getInterfaces()) {
                for (Class<?> foundAlready : interfaces) {
                    if (current.isAssignableFrom(foundAlready)) {
                        continue nextInterface;
                    }
                }
                interfaces.add(current);
            }
            type = type.getSuperclass();
        }
        return interfaces;
    }
}
