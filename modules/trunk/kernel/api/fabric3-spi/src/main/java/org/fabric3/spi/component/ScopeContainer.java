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
package org.fabric3.spi.component;

import java.net.URI;
import java.util.List;

import org.osoa.sca.ConversationEndedException;

import org.fabric3.spi.Lifecycle;
import org.fabric3.scdl.Scope;


/**
 * Manages the lifecycle and visibility of instances associated with a an {@link AtomicComponent}.
 *
 * @version $Rev$ $Date$
 * @param <KEY> the type of IDs that this container uses to identify its contexts. For example, for COMPOSITE scope this could be the URI of the
 * composite component, or for HTTP Session scope it might be the HTTP session ID.
 */
public interface ScopeContainer<KEY> extends Lifecycle {

    /**
     * Returns the Scope that this container supports.
     *
     * @return the Scope that this container supports
     */
    Scope<KEY> getScope();

    /**
     * Registers a component with the scope.
     *
     * @param component the component to register
     */
    void register(AtomicComponent<?> component);

    /**
     * Unregisters a component with the scope.
     *
     * @param component the component to unregister
     */
    void unregister(AtomicComponent<?> component);

    /**
     * Start a new, non-expiring context with the supplied group ID. The context will remain active until explicitly stopped.
     *
     * @param workContext the current WorkContext
     * @param groupId     the group of components to initially associate with this context or null if the scope does not support eager initialization
     * @throws GroupInitializationException if an exception was thrown by any eagerInit component
     */
    void startContext(WorkContext workContext, URI groupId) throws GroupInitializationException;

    /**
     * Start a new context with the supplied group ID which expires according to the given ExpirationPolicy. The context will remain active until it
     * is explicitly stopped or it expires.
     *
     * @param workContext the current WorkContext
     * @param groupId     the group of components to initially associate with this context or null if the scope does not support eager initialization
     * @param policy      determines when the context expires
     * @throws GroupInitializationException if an exception was thrown by any eagerInit component
     */
    public void startContext(WorkContext workContext, URI groupId, ExpirationPolicy policy) throws GroupInitializationException;

    /**
     * Stop the context with the supplied ID.
     *
     * @param workContext the current WorkContext
     */
    void stopContext(WorkContext workContext);

    /**
     * Returns an instance wrapper associated with the current scope context, creating one if necessary
     *
     * @param component   the component
     * @param workContext the work context in which the instance should be obtained
     * @return the wrapper for the target instance
     * @throws TargetResolutionException  if there was a problem instantiating the target instance
     * @throws ConversationEndedException if the instance is conversational and the associated has ended or expired
     */
    <T> InstanceWrapper<T> getWrapper(AtomicComponent<T> component, WorkContext workContext)
            throws TargetResolutionException, ConversationEndedException;

    /**
     * Return a wrapper after use (for example, after invoking the instance).
     *
     * @param component   the component
     * @param workContext the work context returning the instance
     * @param wrapper     the wrapper for the target instance being returned
     * @throws TargetDestructionException if there was a problem returning the target instance
     */
    <T> void returnWrapper(AtomicComponent<T> component, WorkContext workContext, InstanceWrapper<T> wrapper) throws TargetDestructionException;

    /**
     * Initialise an ordered list of components. The list is traversed in order and the getWrapper() method called for each to associate an instance
     * with the supplied context.
     *
     * @param components  the components to be initialized
     * @param groupId     the group ID of the components to initialize
     * @param workContext the work context in which to initialize the components
     * @throws GroupInitializationException if one or more components threw an exception during initialization
     */
    void initializeComponents(List<AtomicComponent<?>> components, URI groupId, WorkContext workContext) throws GroupInitializationException;
}
