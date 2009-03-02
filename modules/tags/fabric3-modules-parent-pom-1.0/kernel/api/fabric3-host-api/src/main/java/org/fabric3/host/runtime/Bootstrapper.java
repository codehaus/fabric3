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

import java.util.Map;

/**
 * Implementations bootstrap a runtime in two phases. The first phase initializes the runtime domain. The second phase initializes the core runtime
 * services.
 *
 * @version $Rev$ $Date$
 */
public interface Bootstrapper {
    /**
     * Initializes the domain for the given runtime.
     *
     * @param runtime          the runtime to initialize the domain for
     * @param bootClassLoader  the bootstrap classloader
     * @param exportedPackages the Java packages exported by the boot contribution
     * @throws InitializationException if there was a problem bootstrapping the runtime
     */
    public void bootRuntimeDomain(Fabric3Runtime<?> runtime,
                                  ClassLoader bootClassLoader,
                                  Map<String, String> exportedPackages) throws InitializationException;

    /**
     * Initialize the core system components for the supplied runtime.
     *
     * @throws InitializationException if there was a problem bootstrapping the runtime
     */
    public void bootSystem() throws InitializationException;

}
