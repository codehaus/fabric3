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
 */
package org.fabric3.fabric.runtime;

import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.component.ScopeRegistry;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.services.componentmanager.ComponentManager;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.services.lcm.LogicalComponentManager;

/**
 * Interface for accessing services provided by a runtime.
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
    ScopeContainer getScopeContainer();

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
