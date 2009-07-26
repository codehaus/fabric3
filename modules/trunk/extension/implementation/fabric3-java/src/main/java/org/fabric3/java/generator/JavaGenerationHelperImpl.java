/*
* Fabric3
* Copyright (c) 2009 Metaform Systems
*
* Fabric3 is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as
* published by the Free Software Foundation, either version 3 of
* the License, or (at your option) any later version, with the
* following exception:
*
* Linking this software statically or dynamically with other
* modules is making a combined work based on this software.
* Thus, the terms and conditions of the GNU General Public
* License cover the whole combination.
*
* As a special exception, the copyright holders of this software
* give you permission to link this software with independent
* modules to produce an executable, regardless of the license
* terms of these independent modules, and to copy and distribute
* the resulting executable under terms of your choice, provided
* that you also meet, for each linked independent module, the
* terms and conditions of the license of that module. An
* independent module is a module which is not derived from or
* based on this software. If you modify this software, you may
* extend this exception to your version of the software, but
* you are not obligated to do so. If you do not wish to do so,
* delete this exception statement from your version.
*
* Fabric3 is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty
* of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU General Public License for more details.
*
* You should have received a copy of the
* GNU General Public License along with Fabric3.
* If not, see <http://www.gnu.org/licenses/>.
*/
package org.fabric3.java.generator;

import java.net.URI;
import java.util.List;
import javax.xml.namespace.QName;

import org.osoa.sca.annotations.Reference;

import org.fabric3.host.Namespaces;
import org.fabric3.java.model.JavaImplementation;
import org.fabric3.java.provision.JavaComponentDefinition;
import org.fabric3.java.provision.JavaSourceDefinition;
import org.fabric3.java.provision.JavaTargetDefinition;
import org.fabric3.model.type.component.AbstractComponentType;
import org.fabric3.model.type.component.CallbackDefinition;
import org.fabric3.model.type.component.ComponentDefinition;
import org.fabric3.model.type.component.Implementation;
import org.fabric3.model.type.component.Scope;
import org.fabric3.model.type.definitions.PolicySet;
import org.fabric3.model.type.java.InjectableAttribute;
import org.fabric3.model.type.java.InjectableAttributeType;
import org.fabric3.model.type.java.InjectingComponentType;
import org.fabric3.model.type.service.ServiceContract;
import org.fabric3.pojo.generator.InstanceFactoryGenerationHelper;
import org.fabric3.pojo.provision.InstanceFactoryDefinition;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalOperation;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalResource;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.physical.InteractionType;
import org.fabric3.spi.policy.Policy;

/**
 * @version $Rev$ $Date$
 */
public class JavaGenerationHelperImpl implements JavaGenerationHelper {
    private static final QName PROPAGATES_CONVERSATION_POLICY = new QName(Namespaces.POLICY, "propagatesConversationPolicy");
    private final InstanceFactoryGenerationHelper helper;

    public JavaGenerationHelperImpl(@Reference InstanceFactoryGenerationHelper helper) {
        this.helper = helper;
    }

    public void generate(JavaComponentDefinition definition, LogicalComponent<? extends JavaImplementation> component) throws GenerationException {
        ComponentDefinition<? extends JavaImplementation> logical = component.getDefinition();
        JavaImplementation implementation = logical.getImplementation();
        InjectingComponentType type = implementation.getComponentType();
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
        definition.setScope(scope);
        definition.setInitLevel(helper.getInitLevel(logical, type));
        definition.setMaxAge(type.getMaxAge());
        definition.setMaxIdleTime(type.getMaxIdleTime());
        definition.setProviderDefinition(providerDefinition);
        helper.processPropertyValues(component, definition);
    }


    public void generateWireSource(JavaSourceDefinition definition, LogicalReference reference, Policy policy) throws GenerationException {
        URI uri = reference.getUri();
        ServiceContract<?> serviceContract = reference.getDefinition().getServiceContract();
        String interfaceName = serviceContract.getQualifiedInterfaceName();

        definition.setUri(uri);
        definition.setValueSource(new InjectableAttribute(InjectableAttributeType.REFERENCE, uri.getFragment()));
        definition.setInterfaceName(interfaceName);
        // assume for now that any wire from a Java component can be optimized
        definition.setOptimizable(true);

        boolean conversational = serviceContract.isConversational();
        List<LogicalOperation> operations = reference.getOperations();
        calculateConversationalPolicy(definition, operations, policy, conversational);
    }

    public void generateCallbackWireSource(JavaSourceDefinition definition, LogicalComponent<? extends JavaImplementation> component,
                                           ServiceContract<?> serviceContract,
                                           Policy policy) throws GenerationException {
        String interfaceName = serviceContract.getQualifiedInterfaceName();
        InjectingComponentType type = component.getDefinition().getImplementation().getComponentType();
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

        definition.setValueSource(new InjectableAttribute(InjectableAttributeType.CALLBACK, name));
        definition.setInterfaceName(interfaceName);
        definition.setUri(URI.create(component.getUri().toString() + "#" + name));
        definition.setOptimizable(false);
    }

    public void generateResourceWireSource(JavaSourceDefinition wireDefinition, LogicalResource<?> resource) throws GenerationException {
        URI uri = resource.getUri();
        ServiceContract<?> serviceContract = resource.getResourceDefinition().getServiceContract();
        String interfaceName = serviceContract.getQualifiedInterfaceName();

        wireDefinition.setUri(uri);
        wireDefinition.setValueSource(new InjectableAttribute(InjectableAttributeType.RESOURCE, uri.getFragment()));
        wireDefinition.setInterfaceName(interfaceName);
    }

    public void generateWireTarget(JavaTargetDefinition definition, LogicalService service) throws GenerationException {
        LogicalComponent<?> component = service.getParent();
        URI uri;
        uri = service.getUri();
        definition.setUri(uri);

        // assume only wires to composite scope components can be optimized
        ComponentDefinition<? extends Implementation<?>> componentDefinition = component.getDefinition();
        Implementation<?> implementation = componentDefinition.getImplementation();
        AbstractComponentType<?, ?, ?, ?> componentType = implementation.getComponentType();
        String scope = componentType.getScope();
        definition.setOptimizable("COMPOSITE".equals(scope));
    }


    /**
     * Determines if the wire propagates conversations. Conversational propagation is handled by the source component.
     *
     * @param definition     the source wire defintion
     * @param operations     the logical operations
     * @param policy         the set of policies for the wire
     * @param conversational true if the contract is conversational
     */
    private void calculateConversationalPolicy(JavaSourceDefinition definition,
                                               List<LogicalOperation> operations,
                                               Policy policy,
                                               boolean conversational) {
        for (LogicalOperation operation : operations) {
            for (PolicySet policySet : policy.getProvidedPolicySets(operation)) {
                if (PROPAGATES_CONVERSATION_POLICY.equals(policySet.getName())) {
                    definition.setInteractionType(InteractionType.PROPAGATES_CONVERSATION);
                    // conversational propagation is for the entire reference so set it an return
                    return;
                }
            }
        }
        if (conversational) {
            definition.setInteractionType(InteractionType.CONVERSATIONAL);
        }

    }


}
