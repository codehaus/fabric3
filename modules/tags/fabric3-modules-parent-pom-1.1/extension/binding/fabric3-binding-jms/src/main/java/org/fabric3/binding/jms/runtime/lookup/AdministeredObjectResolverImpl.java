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

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;

import org.osoa.sca.annotations.Reference;

import org.fabric3.binding.jms.common.ConnectionFactoryDefinition;
import org.fabric3.binding.jms.common.CreateOption;
import org.fabric3.binding.jms.common.DestinationDefinition;
import org.fabric3.binding.jms.runtime.lookup.connectionfactory.ConnectionFactoryStrategy;
import org.fabric3.binding.jms.runtime.lookup.destination.DestinationStrategy;

/**
 * Default implementation of AdministeredObjectResolver.
 *
 * @version $Revision$ $Date$
 */
public class AdministeredObjectResolverImpl implements AdministeredObjectResolver {
    private Map<CreateOption, ConnectionFactoryStrategy> factoryStrategies = new HashMap<CreateOption, ConnectionFactoryStrategy>();
    private Map<CreateOption, DestinationStrategy> destinationStrategies = new HashMap<CreateOption, DestinationStrategy>();


    public AdministeredObjectResolverImpl(@Reference Map<CreateOption, ConnectionFactoryStrategy> factoryStrategies,
                                          @Reference Map<CreateOption, DestinationStrategy> destinationStrategies) {
        this.factoryStrategies = factoryStrategies;
        this.destinationStrategies = destinationStrategies;
    }

    public ConnectionFactory resolve(ConnectionFactoryDefinition definition, Hashtable<String, String> env) throws JmsLookupException {
        CreateOption create = definition.getCreate();
        return factoryStrategies.get(create).getConnectionFactory(definition, env);
    }

    public Destination resolve(DestinationDefinition definition, ConnectionFactory cf, Hashtable<String, String> env) throws JmsLookupException {
        CreateOption create = definition.getCreate();
        return destinationStrategies.get(create).getDestination(definition, cf, env);
    }

}
