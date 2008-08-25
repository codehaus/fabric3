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

import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.component.ScopeRegistry;
import org.fabric3.spi.services.classloading.ClassLoaderRegistry;
import org.fabric3.spi.services.componentmanager.ComponentManager;
import org.fabric3.spi.services.contribution.MetaDataStore;
import org.fabric3.spi.services.lcm.LogicalComponentManager;

/**
 * Interface for accessing primordial services provided by a runtime.
 * <p/>
 * These are the primoridal services that should be provided by all runtime implementations for use by other runtime components.
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
     * Returns the ScopeRegistry used to manage runtime ScopeContainers.
     *
     * @return the ScopeRegistry used to manage runtime ScopeContainers
     */
    ScopeRegistry getScopeRegistry();

    /**
     * Returns the ScopeContainer used to manage runtime component instances.
     *
     * @return the ScopeContainer used to manage runtime component instances
     */
    ScopeContainer<?> getScopeContainer();

    /**
     * Returns the ClassLoaderRegistry used to manage runtime classloaders.
     *
     * @return the ClassLoaderRegistry used to manage runtime classloaders
     */
    ClassLoaderRegistry getClassLoaderRegistry();

    /**
     * Returns the MetaDataStore used to index contribution resources.
     *
     * @return the MetaDataStore used to index contribution resources
     */
    MetaDataStore getMetaDataStore();

}
