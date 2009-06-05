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
package org.fabric3.host.runtime;

/**
 * Implementations manage the Fabric3 runtime lifecycle. This involves transitioning through a series of states:
 * <pre>
 * <ul>
 *      <li>BOOT PRIMORDIAL - the runtime is booted with and its domain containing system components is initialized.
 *      <li>INITIALIZE - extensions are registered and activated in the local runtime domain.
 *      <li>RECOVER - the runtime recovers and synchronizes its state with the application domain.
 *      <li>JOIN DOMIAN - the runtime instance discoveres and joins an application domain.
 *      <li>START - the runtime is prepared to receive and process requests
 *      <li>SHUTDOWN - the runtime has stopped prcessing synnchronous requests and detached from the domain.
 * </ul>
 * </pre>
 *
 * @version $Rev$ $Date$
 */
public interface RuntimeLifecycleCoordinator {

    /**
     * Sets the bootstrap configuration. Thismust be done prior to booting the runtime.
     *
     * @param configuration the bootstrap configuration
     */
    void setConfiguration(BootConfiguration configuration);

    /**
     * Boots the runtime with its primordial components.
     *
     * @throws InitializationException if an error occurs booting the runtime
     */
    void bootPrimordial() throws InitializationException;

    /**
     * Initializes the runtime, including all system components
     *
     * @throws InitializationException if an error occurs initializing the runtime
     */
    void initialize() throws InitializationException;

    /**
     * Performs local recovery operations.
     *
     * @throws InitializationException if an error occurs performing recovery
     */
    void recover() throws InitializationException;

    /**
     * Joins the domain in a non-blocking fashion.
     *
     * @param timeout the timeout in milliseconds or -1 if the operation should wait indefinitely
     * @throws InitializationException if an error occurs joining the domain
     */
    void joinDomain(long timeout) throws InitializationException;

    /**
     * Start the runtime receiving requests.
     *
     * @throws InitializationException if an error occurs starting the runtime
     */
    void start() throws InitializationException;

    /**
     * Shuts the runtime down, stopping it from receiving requests and detaching it from the domain. In-flight synchronous operations will be allowed
     * to proceed to completion.
     *
     * @throws ShutdownException if an error ocurrs shutting down the runtime
     */
    void shutdown() throws ShutdownException;
}


