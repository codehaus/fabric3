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

import java.io.File;
import java.net.URI;

/**
 * Interface that provides information on the host environment. This allows the runtime to access information about the environment in which it is
 * running. The implementation of this interface is provided to the runtime by the host during initialization. Hosts will generally extend this
 * interface to provide additional information.
 *
 * @version $Rev$ $Date$
 */
public interface HostInfo {

    /**
     * Returns the mode the runtime is booted in.
     *
     * @return the mode the runtime is booted in
     */
    RuntimeMode getRuntimeMode();

    /**
     * Returns the SCA domain associated with this runtime. A null domain indicates that this is a standalone runtime with a self-contained domain.
     *
     * @return the SCA domain associated with this runtime; may be null
     */
    URI getDomain();

    /**
     * Gets the base directory for the runtime.
     *
     * @return The base directory for the runtime or null if the runtime does not support persistent capabilities.
     */
    File getBaseDir();

    /**
     * Returns the temporary directory.
     *
     * @return the temporary directory.
     */
    File getTempDir();

    /**
     * Return the value of the named property.
     *
     * @param name         the name of the property
     * @param defaultValue default value to return if the property is not defined
     * @return the value of the named property
     */
    String getProperty(String name, String defaultValue);

    /**
     * True if the host environment supports classloader isolation.
     *
     * @return true if the host environment supports classloader isolation
     */
    boolean supportsClassLoaderIsolation();
}
