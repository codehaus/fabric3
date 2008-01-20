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

/**
 * Interface for mechanisms that are able to bootstrap a runtime.
 *
 * @version $Rev$ $Date$
 */
public interface Bootstrapper {
    /**
     * Bootstrap the supplied runtime and register the primordial system components.
     *
     * @param runtime         the runtime to boot
     * @param bootClassLoader the bootstrap classloader
     * @param appClassLoader  the top-level application classloader
     * @throws InitializationException if there was a problem bootstrapping the runtime
     */
    public void bootPrimordial(Fabric3Runtime<?> runtime, ClassLoader bootClassLoader, ClassLoader appClassLoader)
            throws InitializationException;

    /**
     * Boot and register the system components for the supplied runtime.
     *
     * @param runtime the runtime to boot
     * @throws InitializationException if there was a problem bootstrapping the runtime
     */
    public void bootSystem(Fabric3Runtime<?> runtime) throws InitializationException;

}
