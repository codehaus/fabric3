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
import java.util.Set;
import javax.xml.namespace.QName;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQXAConnectionFactory;
import org.oasisopen.sca.Constants;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Property;

import org.fabric3.binding.jms.common.ConnectionFactoryDefinition;
import org.fabric3.binding.jms.common.CreateOption;
import org.fabric3.binding.jms.common.DestinationDefinition;
import org.fabric3.binding.jms.common.DestinationType;
import org.fabric3.binding.jms.common.JmsBindingMetadata;
import org.fabric3.binding.jms.model.JmsBindingDefinition;
import org.fabric3.spi.binding.provider.BindingMatchResult;
import org.fabric3.spi.binding.provider.BindingProvider;
import org.fabric3.spi.binding.provider.BindingSelectionException;
import org.fabric3.spi.model.instance.LogicalAttachPoint;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalOperation;
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
    // Transacted one way intent

    private static final QName OASIS_TRANSACTED_ONEWAY = new QName(Constants.SCA_NS, "transactedOneWay");
    private static final QName OASIS_TRANSACTED_ONEWAY_GLOBAL = new QName(Constants.SCA_NS, "transactedOneWay.global");

    private String connectionFactory;
    private String xaConnectionFactory;

    @Property
    public void setConnectionFactory(String name) {
        this.connectionFactory = name;
    }

    @Property
    public void setXaConnectionFactory(String name) {
        this.xaConnectionFactory = name;
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
        JmsBindingDefinition referenceDefinition = createBindingDefinition(forwardQueue, false);  // XA not enabled on references
        LogicalBinding<JmsBindingDefinition> referenceBinding = new LogicalBinding<JmsBindingDefinition>(referenceDefinition, source);
        referenceBinding.setAssigned(true);
        QName deployable = source.getParent().getDeployable();
        source.addBinding(referenceBinding);

        boolean xa = isXA(target, false);
        JmsBindingDefinition serviceDefinition = createBindingDefinition(forwardQueue, xa);
        LogicalBinding<JmsBindingDefinition> serviceBinding = new LogicalBinding<JmsBindingDefinition>(serviceDefinition, target, deployable);
        serviceBinding.setAssigned(true);
        target.addBinding(serviceBinding);

        // check if the interface is bidirectional
        if (target.getDefinition().getServiceContract().getCallbackContract() != null) {
            // setup callback bindings
            // derive the callback queue name from the reference name since multiple clients can connect to a service
            String callbackQueue = source.getUri().toString();
            boolean callbackXa = isXA(target, true);
            JmsBindingDefinition callbackReferenceDefinition = createBindingDefinition(callbackQueue, callbackXa);
            LogicalBinding<JmsBindingDefinition> callbackReferenceBinding =
                    new LogicalBinding<JmsBindingDefinition>(callbackReferenceDefinition, source);
            callbackReferenceBinding.setAssigned(true);
            source.addCallbackBinding(callbackReferenceBinding);
            JmsBindingDefinition callbackServiceDefinition = createBindingDefinition(callbackQueue, false); // XA not enabled on service side callback
            LogicalBinding<JmsBindingDefinition> callbackServiceBinding =
                    new LogicalBinding<JmsBindingDefinition>(callbackServiceDefinition, target, deployable);
            callbackServiceBinding.setAssigned(true);
            target.addCallbackBinding(callbackServiceBinding);
            callbackReferenceDefinition.setGeneratedTargetUri(createCallbackUri(source));
            callbackServiceDefinition.setGeneratedTargetUri(createCallbackUri(source));
        }
    }

    private JmsBindingDefinition createBindingDefinition(String queueName, boolean xa) {
        JmsBindingMetadata metadata = new JmsBindingMetadata();

        DestinationDefinition destinationDefinition = new DestinationDefinition();
        destinationDefinition.setDestinationType(DestinationType.queue);
        destinationDefinition.setCreate(CreateOption.ifnotexist);
        destinationDefinition.setName(queueName);
        metadata.setDestination(destinationDefinition);
        if (xa && xaConnectionFactory != null) {
            // XA connection factory defined
            ConnectionFactoryDefinition factoryDefinition = new ConnectionFactoryDefinition();
            factoryDefinition.setName(xaConnectionFactory);
            factoryDefinition.setCreate(CreateOption.never);
            metadata.setConnectionFactory(factoryDefinition);
        } else if (xa) {
            // XA, no connection factory defined
            ConnectionFactoryDefinition factoryDefinition = new ConnectionFactoryDefinition();
            factoryDefinition.setName(ActiveMQXAConnectionFactory.class.getName());
            factoryDefinition.setCreate(CreateOption.always);
            metadata.setConnectionFactory(factoryDefinition);

        } else if (connectionFactory != null) {
            // non-XA connection factory defined
            ConnectionFactoryDefinition factoryDefinition = new ConnectionFactoryDefinition();
            factoryDefinition.setName(connectionFactory);
            factoryDefinition.setCreate(CreateOption.never);
            metadata.setConnectionFactory(factoryDefinition);
        } else {
            // non-XA, no connection factory defined
            ConnectionFactoryDefinition factoryDefinition = new ConnectionFactoryDefinition();
            factoryDefinition.setName(ActiveMQConnectionFactory.class.getName());
            factoryDefinition.setCreate(CreateOption.always);
            metadata.setConnectionFactory(factoryDefinition);
        }
        JmsBindingDefinition definition = new JmsBindingDefinition(metadata, null);
        definition.setMetadata(metadata);
        return definition;
    }


    public URI createCallbackUri(LogicalReference source) {
        LogicalComponent<?> component = source.getParent();
        String name = source.getDefinition().getServiceContract().getCallbackContract().getInterfaceName();
        return URI.create(component.getUri() + "#" + name);
    }

    /**
     * Recurses the component hierarchy to determine if XA transacted messaging is required.
     * <p/>
     * TODO this should be refactored to normalize intents
     *
     * @param attachPoint the service or reference
     * @param callback    true if callback operations should be evaluated
     * @return true if XA is required
     */
    private boolean isXA(LogicalAttachPoint attachPoint, boolean callback) {
        // check operations
        if (callback) {
            for (LogicalOperation operation : attachPoint.getCallbackOperations()) {
                if (containsTransactionIntent(operation.getIntents())) {
                    return true;
                }
            }
        } else {
            for (LogicalOperation operation : attachPoint.getOperations()) {
                if (containsTransactionIntent(operation.getIntents())) {
                    return true;
                }
            }
        }
        // recurse the parents
        LogicalComponent<?> parent = attachPoint.getParent();
        while (parent != null) {
            if (containsTransactionIntent(parent.getIntents())) {
                return true;
            }
            if (containsTransactionIntent(parent.getDefinition().getImplementation().getIntents())) {
                return true;
            }
            parent = parent.getParent();
        }
        return false;

    }

    private boolean containsTransactionIntent(Set<QName> intents) {
        return intents.contains(OASIS_TRANSACTED_ONEWAY_GLOBAL) || intents.contains(OASIS_TRANSACTED_ONEWAY);
    }

}
