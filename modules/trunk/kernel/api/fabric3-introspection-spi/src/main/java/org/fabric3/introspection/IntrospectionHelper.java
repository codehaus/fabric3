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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.Constructor;

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
     * Returns the type of a setter method.
     *
     * @param setter the method to inspect
     * @return the type of value the setter method injects
     * @throws IntrospectionException if there was a problem determining the type
     */
    Type getType(Method setter) throws IntrospectionException;

    /**
     * Returns the type of a method parameter.
     *
     * @param method the method to inspect
     * @param index  the parameter index
     * @return the type of value the method injects
     * @throws IntrospectionException if there was a problem determining the type
     */
    Type getType(Method method, int index) throws IntrospectionException;

    /**
     * Returns the type of a constructor parameter.
     *
     * @param constructor the constructor to inspect
     * @param index  the parameter index
     * @return the type of value the constructor injects
     * @throws IntrospectionException if there was a problem determining the type
     */
    Type getType(Constructor constructor, int index) throws IntrospectionException;
}
