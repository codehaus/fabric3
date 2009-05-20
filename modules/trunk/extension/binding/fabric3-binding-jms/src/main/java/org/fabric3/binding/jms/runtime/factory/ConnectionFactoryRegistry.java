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
package org.fabric3.binding.jms.runtime.factory;

import javax.jms.ConnectionFactory;

/**
 * A registry of JMS connection factories.
 *
 * @version $Revision$ $Date$
 */
public interface ConnectionFactoryRegistry {

    /**
     * Returns the connection factory for the given name.
     *
     * @param name the name the connection factory was registered with
     * @return the connection factory or null if no factory for the name was registered
     */
    ConnectionFactory get(String name);

    /**
     * Registers a connection factory.
     *
     * @param name    the connection factory name
     * @param factory the connection factory
     */
    void register(String name, ConnectionFactory factory);

    /**
     * Removes a registered connection factory.
     *
     * @param name the connection factory name
     */
    void unregister(String name);
}
