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
package org.fabric3.spi.runtime;

import org.fabric3.spi.component.AtomicComponent;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.services.lcm.LogicalComponentManager;
import org.fabric3.spi.services.componentmanager.ComponentManager;
import org.fabric3.spi.services.componentmanager.RegistrationException;

/**
 * Interface for accessing primordial services provided by a runtime.
 * <p/>
 * These are the primoridal services that should be provided by all runtime implementations for use by other runtime
 * components.
 *
 * @version $Rev$ $Date$
 */
public interface RuntimeServices {

    /**
     * Returns this runtime's logical component manager.
     *
     * @return this runtime's logical component manager
     */
    LogicalComponentManager getLogicalComponentManager();

    /**
     * Returns this runtime's physical component manager.
     *
     * @return this runtime's physical component manager
     */
    ComponentManager getComponentManager();

    /**
     * Returns the ScopeContainer used to manage runtime component instances.
     *
     * @return the ScopeContainer used to manage runtime component instances
     */
    ScopeContainer<?> getScopeContainer();

    /**
     * Register a primordial component with the runtime.
     *
     * @param logical  the logical model of the component
     * @param physical the physical container for the component
     * @throws RegistrationException if there was a problem registering the component
     */
    void registerComponent(LogicalComponent<?> logical, AtomicComponent<?> physical) throws RegistrationException;
}
