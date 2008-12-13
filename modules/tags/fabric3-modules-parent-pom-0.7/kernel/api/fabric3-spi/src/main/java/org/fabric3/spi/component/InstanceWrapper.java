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
package org.fabric3.spi.component;

import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.invocation.WorkContext;


/**
 * Provides lifecycle management for an implementation instance associated with an {@link org.fabric3.spi.component.AtomicComponent} for use by the
 * atomic component's associated {@link org.fabric3.spi.component.ScopeContainer}
 *
 * @version $Rev$ $Date$
 */
public interface InstanceWrapper<T> {

    /**
     * Returns the instance managed by this wrapper.
     *
     * @return the instance managed by this wrapper.
     */
    T getInstance();

    /**
     * Returns true if the instance is started.
     *
     * @return true if the instance is started.
     */
    boolean isStarted();

    /**
     * Starts the instance,issuing an initialization callback if the instance is configured to receive one.
     *
     * @param context the current work context
     * @throws InstanceInitializationException
     *          if an error occured starting the instance
     */
    void start(WorkContext context) throws InstanceInitializationException;

    /**
     * Stops the instance, issuing a destruction callback if the instance is configured to receive one..
     *
     * @param context the current work context
     * @throws InstanceDestructionException if an error stopping the instance occurs
     */
    void stop(WorkContext context) throws InstanceDestructionException;

    /**
     * Reinjects updated references on an instance.
     *
     * @throws InstanceLifecycleException if an error occurs during reinjection
     */
    void reinject() throws InstanceLifecycleException;

    /**
     * Adds an object factory for the given reference.
     *
     * @param referenceName the reference
     * @param factory       the object factory
     * @param key           the key associated with the object factory
     */
    void addObjectFactory(String referenceName, ObjectFactory<?> factory, Object key);

}
