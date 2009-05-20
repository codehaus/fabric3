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
 * Resolves administered objects, specifically connection factories and destinations. Different strategies may be used for resolution as defined by
 * ConnectionFactoryDefinition or DestinationDefinition.
 *
 * @version $Revision$ $Date$
 */
public interface AdministeredObjectResolver {

    /**
     * Resolves a ConnectionFactory.
     *
     * @param definition the connection factory definition
     * @param env        properties for use when resolving the ConnectionFactory.
     * @return the connection factory.
     * @throws JmsLookupException if there is an error during resolution
     */
    public ConnectionFactory resolve(ConnectionFactoryDefinition definition, Hashtable<String, String> env) throws JmsLookupException;

    /**
     * Resolves a destination.
     *
     * @param definition the destination definition
     * @param factory    the connection factory
     * @param env        environment properties used during resloution
     * @return the destination
     * @throws JmsLookupException if there is an error during resolution
     */
    Destination resolve(DestinationDefinition definition, ConnectionFactory factory, Hashtable<String, String> env) throws JmsLookupException;

}
