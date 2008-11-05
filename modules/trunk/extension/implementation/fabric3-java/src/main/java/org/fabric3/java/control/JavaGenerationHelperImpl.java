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
package org.fabric3.java.control;

import java.net.URI;

import javax.xml.namespace.QName;

import org.fabric3.java.provision.JavaComponentDefinition;
import org.fabric3.java.provision.JavaWireSourceDefinition;
import org.fabric3.java.provision.JavaWireTargetDefinition;
import org.fabric3.java.scdl.JavaImplementation;
import org.fabric3.pojo.control.InstanceFactoryGenerationHelper;
import org.fabric3.pojo.provision.InstanceFactoryDefinition;
import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.scdl.CallbackDefinition;
import org.fabric3.scdl.ComponentDefinition;
import org.fabric3.scdl.InjectableAttribute;
import org.fabric3.scdl.InjectableAttributeType;
import org.fabric3.scdl.Operation;
import org.fabric3.scdl.Scope;
import org.fabric3.scdl.ServiceContract;
import org.fabric3.scdl.definitions.PolicySet;
import org.fabric3.spi.Namespaces;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalResource;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.physical.InteractionType;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.policy.Policy;
import org.osoa.sca.annotations.Reference;

/**
 * @version $Revision$ $Date$
 */
public class JavaGenerationHelperImpl implements JavaGenerationHelper {
    private static final QName PROPAGATES_CONVERSATION_POLICY = new QName(Namespaces.POLICY, "propagatesConversationPolicy");
    private final InstanceFactoryGenerationHelper helper;

    public JavaGenerationHelperImpl(@Reference InstanceFactoryGenerationHelper helper) {
        this.helper = helper;
    }

    public JavaComponentDefinition generate(LogicalComponent<? extends JavaImplementation> component, JavaComponentDefinition physical)
            throws GenerationException {
        ComponentDefinition<? extends JavaImplementation> logical = component.getDefinition();
        JavaImplementation implementation = logical.getImplementation();
        PojoComponentType type = implementation.getComponentType();
        String scope = type.getScope();

        // create the instance factory definition
        InstanceFactoryDefinition providerDefinition = new InstanceFactoryDefinition();
        providerDefinition.setReinjectable(Scope.COMPOSITE.getScope().equals(scope));
        providerDefinition.setConstructor(type.getConstructor());
        providerDefinition.setInitMethod(type.getInitMethod());
        providerDefinition.setDestroyMethod(type.getDestroyMethod());
        providerDefinition.setImplementationClass(implementation.getImplementationClass());
        helper.processInjectionSites(component, providerDefinition);

        // create the physical component definition
        URI componentId = component.getUri();
        physical.setComponentId(componentId);
        physical.setGroupId(component.getDeployable());
        physical.setScope(scope);
        physical.setInitLevel(helper.getInitLevel(logical, type));
        physical.setMaxAge(type.getMaxAge());
        physical.setMaxIdleTime(type.getMaxIdleTime());
        physical.setProviderDefinition(providerDefinition);
        helper.processPropertyValues(component, physical);
        // generate the classloader resource definition
        URI classLoaderId = component.getClassLoaderId();
        physical.setClassLoaderId(classLoaderId);
        return physical;
    }


    public PhysicalWireSourceDefinition generateWireSource(LogicalComponent<? extends JavaImplementation> source,
                                                           JavaWireSourceDefinition wireDefinition,
                                                           LogicalReference reference,
                                                           Policy policy) throws GenerationException {
        URI uri = reference.getUri();
        ServiceContract<?> serviceContract = reference.getDefinition().getServiceContract();
        String interfaceName = serviceContract.getQualifiedInterfaceName();
        URI classLoaderId = source.getClassLoaderId();

        wireDefinition.setUri(uri);
        wireDefinition.setValueSource(new InjectableAttribute(InjectableAttributeType.REFERENCE, uri.getFragment()));
        wireDefinition.setInterfaceName(interfaceName);
        // assume for now that any wire from a Java component can be optimized
        wireDefinition.setOptimizable(true);

        wireDefinition.setClassLoaderId(classLoaderId);
        calculateConversationalPolicy(wireDefinition, serviceContract, policy);
        return wireDefinition;
    }

    public PhysicalWireSourceDefinition generateCallbackWireSource(LogicalComponent<? extends JavaImplementation> source,
                                                                   JavaWireSourceDefinition wireDefinition,
                                                                   ServiceContract<?> serviceContract,
                                                                   Policy policy) throws GenerationException {
        String interfaceName = serviceContract.getQualifiedInterfaceName();
        URI classLoaderId = source.getClassLoaderId();
        PojoComponentType type = source.getDefinition().getImplementation().getComponentType();
        String name = null;
        for (CallbackDefinition entry : type.getCallbacks().values()) {
            // NB: This currently only supports the case where one callback injection site of the same type is on an implementation.
            // TODO clarify with the spec if having more than one callback injection site of the same type is valid
            if (entry.getServiceContract().isAssignableFrom(serviceContract)) {
                name = entry.getName();
                break;
            }
        }
        if (name == null) {
            String interfaze = serviceContract.getQualifiedInterfaceName();
            throw new CallbackSiteNotFound("Callback injection site not found for type: " + interfaze, interfaze);
        }

        wireDefinition.setValueSource(new InjectableAttribute(InjectableAttributeType.CALLBACK, name));
        wireDefinition.setInterfaceName(interfaceName);
        wireDefinition.setUri(URI.create(source.getUri().toString() + "#" + name));
        wireDefinition.setOptimizable(false);
        wireDefinition.setClassLoaderId(classLoaderId);
        return wireDefinition;
    }

    public PhysicalWireSourceDefinition generateResourceWireSource(LogicalComponent<? extends JavaImplementation> source,
                                                                   LogicalResource<?> resource,
                                                                   JavaWireSourceDefinition wireDefinition) throws GenerationException {
        URI uri = resource.getUri();
        ServiceContract<?> serviceContract = resource.getResourceDefinition().getServiceContract();
        String interfaceName = serviceContract.getQualifiedInterfaceName();
        URI classLoaderId = source.getClassLoaderId();

        wireDefinition.setUri(uri);
        wireDefinition.setValueSource(new InjectableAttribute(InjectableAttributeType.RESOURCE, uri.getFragment()));
        wireDefinition.setClassLoaderId(classLoaderId);
        wireDefinition.setInterfaceName(interfaceName);
        return wireDefinition;
    }

    public PhysicalWireTargetDefinition generateWireTarget(LogicalService service,
                                                           LogicalComponent<? extends JavaImplementation> target,
                                                           JavaWireTargetDefinition wireDefinition,
                                                           Policy policy) throws GenerationException {
        URI uri;
        if (service != null) {
            uri = service.getUri();
        } else {
            // no service specified, use the default
            uri = target.getUri();
        }
        wireDefinition.setUri(uri);
        URI classLoaderId = target.getClassLoaderId();
        wireDefinition.setClassLoaderId(classLoaderId);

        // assume for now that only wires to composite scope components can be optimized
        String scope = target.getDefinition().getImplementation().getComponentType().getScope();
        wireDefinition.setOptimizable("COMPOSITE".equals(scope));
        return wireDefinition;
    }


    /**
     * Determines if the wire propagates conversations. Conversational propagation is handled by the source component.
     *
     * @param wireDefinition  the source wire defintion
     * @param serviceContract the wire service cotnract
     * @param policy          the set of policies for the wire
     */
    private void calculateConversationalPolicy(JavaWireSourceDefinition wireDefinition, ServiceContract<?> serviceContract, Policy policy) {
        for (Operation<?> operation : serviceContract.getOperations()) {
            for (PolicySet policySet : policy.getProvidedPolicySets(operation)) {
                if (PROPAGATES_CONVERSATION_POLICY.equals(policySet.getName())) {
                    wireDefinition.setInteractionType(InteractionType.PROPAGATES_CONVERSATION);
                    // conversational propagation is for the entire reference so set it an return
                    return;
                }
            }
        }
        if (serviceContract.isConversational()) {
            wireDefinition.setInteractionType(InteractionType.CONVERSATIONAL);
        }

    }


}
