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

import java.net.URI;
import javax.xml.namespace.QName;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Property;

import org.fabric3.binding.jms.common.ConnectionFactoryDefinition;
import org.fabric3.binding.jms.common.CreateOption;
import org.fabric3.binding.jms.common.DestinationDefinition;
import org.fabric3.binding.jms.common.DestinationType;
import org.fabric3.binding.jms.common.JmsBindingMetadata;
import org.fabric3.binding.jms.scdl.JmsBindingDefinition;
import org.fabric3.spi.binding.provider.BindingMatchResult;
import org.fabric3.spi.binding.provider.BindingProvider;
import org.fabric3.spi.binding.provider.BindingSelectionException;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
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

    public QName getType() {
        return JmsBindingDefinition.BINDING_QNAME;
    }

    public BindingMatchResult canBind(LogicalReference source, LogicalService target) {
        // TODO handle must provide intents
        return new BindingMatchResult(true, getType());
    }

    public void bind(LogicalReference source, LogicalService target) throws BindingSelectionException {
        // setup forward bindings
        // derive the forward queue name from the service name
        String forwardQueue = target.getUri().toString();
        JmsBindingDefinition definition = createBindingDefinition(forwardQueue);
        LogicalBinding<JmsBindingDefinition> referenceBinding = new LogicalBinding<JmsBindingDefinition>(definition, source);
        referenceBinding.setAssigned(true);
        QName deployable = source.getParent().getDeployable();
        source.addBinding(referenceBinding);
        LogicalBinding<JmsBindingDefinition> serviceBinding = new LogicalBinding<JmsBindingDefinition>(definition, target, deployable);
        serviceBinding.setAssigned(true);
        target.addBinding(serviceBinding);

        // check if the interface is bidirectional
        if (target.getDefinition().getServiceContract().getCallbackContract() != null) {
            // setup callback bindings
            // derive the callback queue name from the reference name since multiple clients can connect to a service
            String callbackQueue = source.getUri().toString();
            JmsBindingDefinition callbackDefinition = createBindingDefinition(callbackQueue);
            LogicalBinding<JmsBindingDefinition> callbackReferenceBinding = new LogicalBinding<JmsBindingDefinition>(callbackDefinition, source);
            callbackReferenceBinding.setAssigned(true);
            source.addCallbackBinding(callbackReferenceBinding);
            LogicalBinding<JmsBindingDefinition> callbackServiceBinding =
                    new LogicalBinding<JmsBindingDefinition>(callbackDefinition, target, deployable);
            callbackServiceBinding.setAssigned(true);
            target.addCallbackBinding(callbackServiceBinding);
            callbackDefinition.setGeneratedTargetUri(createCallbackUri(source));
        }
    }

    private JmsBindingDefinition createBindingDefinition(String queueName) {
        JmsBindingMetadata metadata = new JmsBindingMetadata();

        DestinationDefinition destinationDefinition = new DestinationDefinition();
        destinationDefinition.setDestinationType(DestinationType.queue);
        destinationDefinition.setCreate(CreateOption.ifnotexist);
        destinationDefinition.setName(queueName);
        metadata.setDestination(destinationDefinition);

        ConnectionFactoryDefinition factoryDefinition = new ConnectionFactoryDefinition();
        factoryDefinition.setName(ActiveMQConnectionFactory.class.getName());
        factoryDefinition.setCreate(CreateOption.ifnotexist);
        factoryDefinition.addProperty("brokerURL", brokerUrl);
        metadata.setConnectionFactory(factoryDefinition);

        JmsBindingDefinition definition = new JmsBindingDefinition(metadata, null);
        definition.setMetadata(metadata);
        return definition;
    }


    public URI createCallbackUri(LogicalReference source) {
        LogicalComponent<?> component = source.getParent();
        String name = source.getDefinition().getServiceContract().getCallbackContract().getInterfaceName();
        return URI.create(component.getUri() + "#" + name);
    }


}
