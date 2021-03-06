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
package org.fabric3.spi.services.runtime;

import java.net.URI;

import org.fabric3.spi.model.topology.RuntimeInfo;

/**
 * Provides the abstraction to the component that provides information about the local runtime.
 * <p/>
 * Information about the runtime includes,
 * <p/>
 * <li>Runtime Id</li> <li>Components that are running in the runtime</li> <li>Features that are supported by the runtime</li>
 *
 * @version $Revsion$ $Date$
 */
public interface RuntimeInfoService {

    /**
     * Returns the current rumtime id.
     *
     * @return the current rumtime id
     */
    URI getCurrentRuntimeId();

    /**
     * Returns the information on the current runtime.
     *
     * @return Information on the current runtime.
     */
    RuntimeInfo getRuntimeInfo();

    /**
     * Register the opaque destination identifier to contact this runtime on. The identifier is registered by the participant messaging
     * infrastructure.
     *
     * @param id the opaque identifier
     */
    void registerMessageDestination(String id);
}
