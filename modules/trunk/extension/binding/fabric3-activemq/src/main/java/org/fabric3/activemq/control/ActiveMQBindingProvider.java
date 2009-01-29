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
package org.fabric3.activemq.control;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Property;

import org.fabric3.binding.jms.common.ConnectionFactoryDefinition;
import org.fabric3.binding.jms.common.CreateOption;
import org.fabric3.binding.jms.common.DestinationDefinition;
import org.fabric3.binding.jms.common.DestinationType;
import org.fabric3.binding.jms.common.JmsBindingMetadata;
import org.fabric3.binding.jms.scdl.JmsBindingDefinition;
import org.fabric3.spi.binding.BindingProvider;
import org.fabric3.spi.binding.BindingSelectionException;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;

/**
 * Allows ActiveMQ to be used for sca.binding in a domain. By default, this provider configures a wire to use an embedded broker, which forwards
 * messages to external brokers with target service consumers. To configure the wire to use a remote broker topology, the <code>brokerUrl</code>
 * property may be set to the appropriate broker location.
 *
 * @version $Revision$ $Date$
 */
@EagerInit
public class ActiveMQBindingProvider implements BindingProvider {
    private String brokerUrl = "vm://DefaultBroker";

    @Property
    public void setBrokerUrl(String brokerUrl) {
        this.brokerUrl = brokerUrl;
    }

    public MatchType canBind(LogicalReference source, LogicalService target) {
        // TODO handle must provide intents
        return MatchType.REQUIRED_INTENTS;
    }

    public void bind(LogicalReference source, LogicalService target) throws BindingSelectionException {
        JmsBindingMetadata metadata = new JmsBindingMetadata();

        DestinationDefinition destinationDefinition = new DestinationDefinition();
        destinationDefinition.setDestinationType(DestinationType.queue);
        destinationDefinition.setCreate(CreateOption.always);
        destinationDefinition.setName(target.getUri().toString());
        metadata.setDestination(destinationDefinition);

        ConnectionFactoryDefinition factoryDefinition = new ConnectionFactoryDefinition();
        factoryDefinition.setName(ActiveMQConnectionFactory.class.getName());
        factoryDefinition.setCreate(CreateOption.always);
        factoryDefinition.addProperty("brokerURL", brokerUrl);
        metadata.setConnectionFactory(factoryDefinition);

        JmsBindingDefinition definition = new JmsBindingDefinition(metadata, null);
        definition.setMetadata(metadata);

        LogicalBinding<JmsBindingDefinition> referenceBinding = new LogicalBinding<JmsBindingDefinition>(definition, source);
        source.addBinding(referenceBinding);

        LogicalBinding<JmsBindingDefinition> serviceBinding = new LogicalBinding<JmsBindingDefinition>(definition, target);
        target.addBinding(serviceBinding);

    }


}
