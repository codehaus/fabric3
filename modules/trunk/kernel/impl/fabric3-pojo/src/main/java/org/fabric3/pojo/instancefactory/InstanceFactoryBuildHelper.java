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
package org.fabric3.pojo.instancefactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.fabric3.model.type.java.Signature;

/**
 * Utility methods used when creating ReflectiveInstanceFactoryProviders.
 *
 * @version $Rev$ $Date$
 */
public interface InstanceFactoryBuildHelper {

    /**
     * Loads the class using the given classloader
     *
     * @param cl   the classloader to load the class with
     * @param name the name of the class
     * @return the loaded class
     * @throws ClassNotFoundException if the class is not accessible to the classloader
     */
    Class<?> loadClass(ClassLoader cl, String name) throws ClassNotFoundException;

    /**
     * Returns the constructor on the given class matching the signature.
     *
     * @param implClass the class
     * @param signature the constructor signature
     * @return the constructor
     * @throws ClassNotFoundException if one of the constructor parameters could not be loaded
     * @throws NoSuchMethodException  if no matching constructor could be found
     */
    <T> Constructor<T> getConstructor(Class<T> implClass, Signature signature) throws ClassNotFoundException, NoSuchMethodException;

    /**
     * Returns the method on the given class matching the signature.
     *
     * @param implClass the class
     * @param signature the method signature
     * @return the constructor
     * @throws ClassNotFoundException if one of the method parameters could not be loaded
     * @throws NoSuchMethodException  if no matching method could be found
     */
    Method getMethod(Class<?> implClass, Signature signature) throws NoSuchMethodException, ClassNotFoundException;

}
