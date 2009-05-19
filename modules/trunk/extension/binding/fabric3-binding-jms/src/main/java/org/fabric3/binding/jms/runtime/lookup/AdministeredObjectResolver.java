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
package org.fabric3.binding.jms.runtime.lookup;

import java.util.Hashtable;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;

import org.fabric3.binding.jms.common.ConnectionFactoryDefinition;
import org.fabric3.binding.jms.common.DestinationDefinition;

/**
 * @version $Revision$ $Date$
 */
public interface AdministeredObjectResolver {

    /**
     * Resolves a connection factory.
     *
     * @param definition the factory definition
     * @param env        environment properties used during resloution
     * @return the connection factory
     * @throws JmsLookupException if there is an error during resolution
     */
    ConnectionFactory resolve(ConnectionFactoryDefinition definition, Hashtable<String, String> env) throws JmsLookupException;

    void release(ConnectionFactoryDefinition definition);

    Destination resolve(DestinationDefinition definition, ConnectionFactory cf, Hashtable<String, String> env) throws JmsLookupException;

}
