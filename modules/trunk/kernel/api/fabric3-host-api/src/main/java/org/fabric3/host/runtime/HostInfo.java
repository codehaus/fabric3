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
package org.fabric3.host.runtime;

import java.net.URI;
import java.net.URL;

/**
 * Interface that provides information on the host environment. This allows the runtime to access information about the
 * environment in which it is running. The implementation of this interface is provided to the runtime by the host
 * during initialization. Hosts will generally extend this interface to provide additional information.
 *
 * @version $Rev$ $Date$
 */
public interface HostInfo {

    /**
     * Returns the SCA domain associated with this runtime. A null domain indicates that this is a standalone runtime
     * with a self-contained assembly.
     *
     * @return the SCA domain associated with this runtime; may be null
     */
    URI getDomain();

    /**
     * Returns the unique runtime id in the SCA domain.
     *
     * @return the SCA domain associated with this runtime; may be null
     */
    URI getRuntimeId();

    /**
     * Gets the base URL for the runtime.
     *
     * @return The base URL for the runtime.
     */
    URL getBaseURL();

    /**
     * Returns whether the host considers itself "online" or connected to the internet. This can be used by services to
     * enable access to remote resources.
     *
     * @return true if the host is online.
     */
    boolean isOnline();

    /**
     * Return the value of the named property.
     *
     * @param name         the name of the property
     * @param defaultValue default value to return if the property is not defined
     * @return the value of the named property
     */
    String getProperty(String name, String defaultValue);

}
