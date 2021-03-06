/*
 * Fabric3
 * Copyright � 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the �License�), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an �as is� basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
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
     * @throws WriteException if an error occurs replacing the root component
     */
    void replaceRootComponent(LogicalCompositeComponent component) throws WriteException;

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
     * @throws ReadException if there was a problem initializing the components
     */
    void initialize() throws ReadException;


}
