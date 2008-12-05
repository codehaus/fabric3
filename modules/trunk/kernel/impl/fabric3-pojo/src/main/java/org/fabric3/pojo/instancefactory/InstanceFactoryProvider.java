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

import java.lang.reflect.Type;

import org.fabric3.model.type.java.InjectableAttribute;
import org.fabric3.spi.ObjectFactory;

/**
 * @version $Rev$ $Date$
 */
public interface InstanceFactoryProvider<T> {
    /**
     * Return the implementation class.
     *
     * @return the implementation class.
     */
    Class<T> getImplementationClass();

    /**
     * Sets an object factory for an injection site.
     *
     * @param name          the injection site name
     * @param objectFactory the object factory
     */
    void setObjectFactory(InjectableAttribute name, ObjectFactory<?> objectFactory);

    /**
     * Sets an object factory for an injection site.
     *
     * @param attribute the injection site
     * @param objectFactory       the object factory
     * @param key                 the key for Map-based injection sites
     */
    void setObjectFactory(InjectableAttribute attribute, ObjectFactory<?> objectFactory, Object key);

    /**
     * Returns a previously added object factory for the injection site.
     *
     * @param attribute the injection site
     * @return the object factory or null
     */
    ObjectFactory<?> getObjectFactory(InjectableAttribute attribute);

    /**
     * Returns the type for the injection site
     *
     * @param attribute the injection site
     * @return the required type
     */
    Class<?> getMemberType(InjectableAttribute attribute);

    /**
     * Returns the generic type for the injection site
     *
     * @param attribute the injection site
     * @return the required type
     */
    Type getGenericType(InjectableAttribute attribute);

    /**
     * Create an instance factory that can be used to create component instances.
     *
     * @return a new instance factory
     */
    InstanceFactory<T> createFactory();
}
