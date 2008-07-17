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
package org.fabric3.host.runtime;

import java.util.concurrent.Future;

/**
 * Implementations manage the lifecycle for a runtime type. This involves transitioning through a series of states:
 * <pre>
 * <ul>
 *      <li>BOOT PRIMORDIAL - the runtime is booted with primordial system components.
 *      <li>INITIALIZE - extensions are registered and activated in the local runtime domain.
 *      <li>JOIN DOMIAN - the runtime instance discoveres and joins an application domain.
 *      <li>RECOVER - the runtime recovers and synchronizes its state with the application domain.
 *      <li>START - the runtime is prepared to receive and process requests
 *      <li>SHUTDOWN - the runtime has stopped prcessing synnchronous requests and detached from the domain.
 * </ul>
 * </pre>
 * The initialize operation is synchronous while all other operations are performed in non-blocking fashion. For
 * non-blocking transitions, host environments may choose to block on the returned Future or perform additional work
 * prior to querying for completion.
 *
 * @version $Rev$ $Date$
 */
public interface RuntimeLifecycleCoordinator<R extends Fabric3Runtime, B extends Bootstrapper> {

    /**
     * Boots the runtime with its primordial components.
     *
     * @param runtime         the runtime to boot
     * @param bootstrapper    the bootstrapper to bootstrap primordial components and system components
     * @param bootClassLoader the bootstrap classloader
     * @param appClassLoader  the top-level application classloader
     * @throws InitializationException if an error occurs booting the runtime
     */
    void bootPrimordial(R runtime, B bootstrapper, ClassLoader bootClassLoader, ClassLoader appClassLoader)
            throws InitializationException;

    /**
     * Initializes the local runtime, including all system components
     *
     * @throws InitializationException if an error occurs initializing the runtime
     */
    void initialize() throws InitializationException;


    /**
     * Join the domain in a non-blocking fashion.
     *
     * @param timeout the timeout in milliseconds or -1 if the operation should wait indefinitely
     * @return a future that can be polled for completion of the operation
     */
    Future<Void> joinDomain(long timeout);

    /**
     * Perform the recovery operation. On controller nodes, this may result in reprovisioning components and resources.
     *
     * @return a future that can be polled for completion of the operation
     */
    Future<Void> recover();

    /**
     * Start the runtime receiving requests.
     *
     * @return a future that can be polled for completion of the operation
     * @throws StartException if an error starting the runtime occurs
     */
    Future<Void> start() throws StartException;

    /**
     * Shuts the runtime down, stopping it from receiving requests and detaching it from the domain. In-flight
     * synchronous operations will be allowed to proceed to completion.
     *
     * @return a future that can be polled for completion of the operation
     * @throws ShutdownException if an error ocurrs shutting down the runtime
     */
    Future<Void> shutdown() throws ShutdownException;
}


