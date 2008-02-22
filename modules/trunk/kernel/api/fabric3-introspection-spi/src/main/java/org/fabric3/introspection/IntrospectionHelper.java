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
package org.fabric3.introspection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Set;

import org.fabric3.scdl.InjectableAttributeType;

/**
 * Helper service that provides support methods to simplify introspection.
 *
 * @version $Rev$ $Date$
 */
public interface IntrospectionHelper {
    /**
     * Derive the name of an injection site from a field.
     *
     * @param field    the field to inspect
     * @param override an override specified in an annotation
     * @return the name of the injection site
     * @throws IntrospectionException if the field is not a valid injection site
     */
    String getSiteName(Field field, String override) throws IntrospectionException;

    /**
     * Derive the name of an injection site from a setter method.
     *
     * @param setter   the setter method to inspect
     * @param override an override specified in an annotation
     * @return the name of the injection site
     * @throws IntrospectionException if the method is not a valid injection site
     */
    String getSiteName(Method setter, String override) throws IntrospectionException;

    /**
     * Derive the name of an injection site from a setter method.
     *
     * @param constructor the constructor to inspect
     * @param index       the index of the constructor parameter to inspect
     * @param override    an override specified in an annotation
     * @return the name of the injection site
     * @throws IntrospectionException if the method is not a valid injection site
     */
    String getSiteName(Constructor<?> constructor, int index, String override) throws IntrospectionException;

    /**
     * Returns the generic type of a setter method.
     *
     * @param setter the method to inspect
     * @return the type of value the setter method injects
     * @throws IntrospectionException if there was a problem determining the type
     */
    Type getGenericType(Method setter) throws IntrospectionException;

    /**
     * Returns the generic type of a method parameter.
     *
     * @param method the method to inspect
     * @param index  the parameter index
     * @return the type of value the method injects
     * @throws IntrospectionException if there was a problem determining the type
     */
    Type getGenericType(Method method, int index) throws IntrospectionException;

    /**
     * Returns the generic type of a constructor parameter.
     *
     * @param constructor the constructor to inspect
     * @param index       the parameter index
     * @return the type of value the constructor injects
     * @throws IntrospectionException if there was a problem determining the type
     */
    Type getGenericType(Constructor<?> constructor, int index) throws IntrospectionException;

    /**
     * Returns the raw type of a setter method.
     *
     * @param setter the method to inspect
     * @return the type of value the setter method injects
     * @throws IntrospectionException if there was a problem determining the type
     */
    Class<?> getType(Method setter) throws IntrospectionException;

    /**
     * Returns the raw type of a method parameter.
     *
     * @param method the method to inspect
     * @param index  the parameter index
     * @return the type of value the method injects
     * @throws IntrospectionException if there was a problem determining the type
     */
    Class<?> getType(Method method, int index) throws IntrospectionException;

    /**
     * Returns the raw type of a constructor parameter.
     *
     * @param constructor the constructor to inspect
     * @param index       the parameter index
     * @return the type of value the constructor injects
     * @throws IntrospectionException if there was a problem determining the type
     */
    Class<?> getType(Constructor<?> constructor, int index) throws IntrospectionException;

    /**
     * Returns true if the supplied type should be treated as many-valued.
     * <p/>
     * This is generally true for arrays, Collection or Map types.
     *
     * @param typeMapping the mapping to use to resolve any formal types
     * @param type        the type to check
     * @return true if the type should be treated as many-valued
     */
    boolean isManyValued(TypeMapping typeMapping, Type type);

    Class<?> getRawType(Type type);

    InjectableAttributeType inferType(Type type);

    /**
     * Determine if an annotation is present on this interface or any superinterface.
     * <p/>
     * This is similar to the use of @Inherited on classes (given @Inherited does not apply to interfaces).
     *
     * @param type           the interface to check
     * @param annotationType the annotation to look for
     * @return true if the annotation is present
     */
    boolean isAnnotationPresent(Class<?> type, Class<? extends Annotation> annotationType);

    /**
     * Map the formal parameters of a type, its superclass and superinterfaces to the actual parameters of the class.
     *
     * @param type the class whose parameters should be mapped
     * @return a mapping of formal type parameters to actual types
     */
    TypeMapping mapTypeParameters(Class<?> type);

    /**
     * Returns the base type for the supplied type.
     * <p/>
     * The base type is the actual type of a property or reference having removed any decoration for arrays or collections.
     *
     * @param type the type of a field or parameter
     * @return the actual type of the property or reference corresponding to the parameter
     */
    Type getBaseType(Type type);

    /**
     * Returns all interfaces directly implemented by this class or any superclass.
     * <p/>
     * Class#getInterfaces only returns interfaces directly implemented by the class. This method returns all interfaces including those implemented
     * by any superclasses. It excludes interfaces that are super-interfaces of those implemented by subclasses.
     *
     * @param type the class whose interfaces should be returned
     * @return the unique interfaces immplemented by that class
     */
    Set<Class<?>> getImplementedInterfaces(Class<?> type);
}
