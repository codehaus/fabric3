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
package org.fabric3.spi.component;

/**
 * Interface implemented by services that are able to store InstanceWrappers between invocations.
 * Instances are grouped together into collections identified by the context id. Each collection may contain
 * instances from several components.
 *
 * @version $Rev$ $Date$
 * @param <KEY> the type of key this store uses to identify contexts
 */
public interface InstanceWrapperStore<KEY> {
    /**
     * Notification to the store that a scope context is being started.
     * This must be called before any instances are associated with the context
     *
     * @param contextId the id of the context
     * @throws StoreException if there was a problem initializing the context
     */
    void startContext(KEY contextId) throws StoreException;

    /**
     * Notification to the store that a scope context is ending.
     * 
     * @param contextId the id of the context
     * @throws StoreException if there was a problem shutting the context down
     */
    void stopContext(KEY contextId) throws StoreException;

    /**
     * Get the instance of the supplied component that is associated with the supplied context.
     * Returns null if there is no instance currently associated.
     *
     * @param component the component whose instance should be returned
     * @param contextId the context whose instance should be returned
     * @return the wrapped instance associated with the context or null
     * @throws StoreException if there was problem returning the instance
     */
    <T> InstanceWrapper<T> getWrapper(AtomicComponent<T> component, KEY contextId) throws StoreException;

    /**
     * Associated an instance of the supplied component with the supplied context.
     *
     * @param component the component whose instance is being stored
     * @param contextId the context with which the instance is associated
     * @param wrapper the wrapped instance
     * @throws StoreException if there was a problem storing the instance
     */
    <T> void putWrapper(AtomicComponent<T> component, KEY contextId, InstanceWrapper<T> wrapper) throws StoreException;
}
