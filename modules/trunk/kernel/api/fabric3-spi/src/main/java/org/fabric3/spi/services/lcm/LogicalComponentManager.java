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
package org.fabric3.spi.services.lcm;

import java.net.URI;
import java.util.Collection;

import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;

/**
 * Manages logical components in a domain. There is one LogicalComponentManager per domain. Implementations may bve transient or persistent.
 *
 * @version $Revision$ $Date$
 */
public interface LogicalComponentManager {

    /**
     * Returns the root component in the domain.
     *
     * @return the root component in the domain.
     */
    LogicalCompositeComponent getRootComponent();

    /**
     * Replaces the root component in the domain. This is generally used during deployment to update the domain with a modified copy of the logical
     * model.
     *
     * @param component the replacement
     * @throws StoreException if an error occurs replacing the root component
     */
    void replaceRootComponent(LogicalCompositeComponent component) throws StoreException;

    /**
     * Returns the component uniquely identified by an id.
     *
     * @param uri the unique id of the component
     * @return the component uniquely identified by an id, or null
     */
    LogicalComponent<?> getComponent(URI uri);

    /**
     * Gets the top level logical components in the domain (the immediate children of the domain component).
     *
     * @return the top level components in the domain
     */
    Collection<LogicalComponent<?>> getComponents();

    /**
     * Initializes the manager.
     *
     * @throws RecoveryException if there was a problem initializing the components
     */
    void initialize() throws RecoveryException;


}
