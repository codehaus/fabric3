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
package org.fabric3.xquery.control;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;
import org.w3c.dom.Document;

import org.fabric3.model.type.component.ComponentDefinition;
import org.fabric3.model.type.component.ComponentReference;
import org.fabric3.model.type.component.ComponentService;
import org.fabric3.model.type.service.Operation;
import org.fabric3.model.type.service.ServiceContract;
import org.fabric3.model.type.component.Property;
import org.fabric3.model.type.component.ReferenceDefinition;
import org.fabric3.model.type.component.ServiceDefinition;
import org.fabric3.spi.generator.ComponentGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.generator.GeneratorRegistry;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalResource;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.instance.LogicalWire;
import org.fabric3.spi.model.physical.InteractionType;
import org.fabric3.spi.model.physical.PhysicalComponentDefinition;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.policy.Policy;
import org.fabric3.spi.contribution.ContributionUriEncoder;
import org.fabric3.spi.util.UriHelper;
import org.fabric3.xquery.provision.XQueryComponentDefinition;
import org.fabric3.xquery.provision.XQueryComponentWireSourceDefinition;
import org.fabric3.xquery.provision.XQueryComponentWireTargetDefinition;
import org.fabric3.xquery.scdl.XQueryComponentType;
import org.fabric3.xquery.scdl.XQueryImplementation;
import org.fabric3.xquery.scdl.XQueryProperty;
import org.fabric3.xquery.scdl.XQueryServiceContract;

/**
 * @version $Rev$ $Date$
 */
@EagerInit
public class XQueryComponentGenerator implements ComponentGenerator<LogicalComponent<XQueryImplementation>> {

    private final GeneratorRegistry registry;
    private ContributionUriEncoder encoder;

    public XQueryComponentGenerator(@Reference GeneratorRegistry registry) {
        this.registry = registry;
    }

    @Init
    public void init() {
        registry.register(XQueryImplementation.class, this);
    }

    public PhysicalComponentDefinition generate(LogicalComponent<XQueryImplementation> component) throws GenerationException {
        ComponentDefinition<XQueryImplementation> definition = component.getDefinition();
        XQueryComponentDefinition physical = new XQueryComponentDefinition();
        physical.setLocation(definition.getImplementation().getLocation());
        physical.setContext(definition.getImplementation().getContext());
        processPropertyValues(component, physical);
        refineServiceContracts(component, physical);
        //create the functionDefinitions for services and references as well as property mapping

        return physical;
    }

    private void refineServiceContracts(LogicalComponent<XQueryImplementation> component, XQueryComponentDefinition physical) {
        Map<String, List<QName>> serviceFunctions = new HashMap<String, List<QName>>();
        Map<String, List<QName>> referenceFunctions = new HashMap<String, List<QName>>();
        Map<String, List<QName>> callbackFunctions = new HashMap<String, List<QName>>();
        //Map<String, List<QName>> referenceCallbackFunctions = new HashMap<String, List<QName>>();
        Map<String, ServiceContract> references = new HashMap<String, ServiceContract>();
        Map<String, ServiceContract> services = new HashMap<String, ServiceContract>();

        //TODO need to find a more optimal way to dynamically override service contracts
        //This builds up a map of service and reference service contracts to be used in case
        //the definition does not explicitly set one (the XQueryService contract can be too generic)
        for (LogicalComponent lc : component.getParent().getComponents()) {
            for (LogicalReference lr : (Collection<LogicalReference>) lc.getReferences()) {
                for (LogicalWire wire : component.getParent().getWires(lr)) {
                    URI targetUri = UriHelper.getDefragmentedName(wire.getTargetUri());
                    if (component.getUri().equals(targetUri)) {
                        String serviceName = wire.getTargetUri().getFragment();
                        services.put(serviceName, lr.getDefinition().getServiceContract());
                    }
                }
            }
        }

        for (LogicalReference lr : (Collection<LogicalReference>) component.getReferences()) {
            for (LogicalWire wire : component.getParent().getWires(lr)) {
                URI sourceUri = UriHelper.getDefragmentedName(wire.getTargetUri());
                LogicalComponent lc = component.getParent().getComponent(sourceUri);
                String referenceName = wire.getTargetUri().getFragment();
                references.put(referenceName, lc.getService(referenceName).getDefinition().getServiceContract());
            }
        }

        ComponentDefinition<XQueryImplementation> definition = component.getDefinition();

        for (Map.Entry<String, ServiceDefinition> entry : definition.getImplementation().getComponentType().getServices().entrySet()) {
            String serviceName = entry.getKey();
            XQueryServiceContract service = (XQueryServiceContract) entry.getValue().getServiceContract();
            if (service.getQname() == null && "XQueryService".equals(serviceName)) {
                continue;
            }
            addFunctions(entry.getKey(), service, serviceFunctions);
            if (service.getCallbackContract() != null) {
                XQueryServiceContract callback = (XQueryServiceContract) service.getCallbackContract();
                addFunctions(callback.getQname().getLocalPart(), callback, callbackFunctions);
            }
            ComponentService compService = definition.getServices().get(serviceName);
            if (compService != null && compService.getServiceContract() != null) { //override the ServiceContract with a more specific type
                entry.getValue().setServiceContract(compService.getServiceContract());
            } else {//not explicitly set, obtain the reference to the service from the composite if available
                ServiceContract contract = services.get(serviceName);
                if (contract != null) {
                    entry.getValue().setServiceContract(contract);
                } else {
                    //System.out.println("Warning, unable to refine contract " + entry.getValue().getName());
                }
            }
        }
        for (Map.Entry<String, ReferenceDefinition> entry : definition.getImplementation().getComponentType().getReferences().entrySet()) {
            String referenceName = entry.getKey();
            XQueryServiceContract reference = (XQueryServiceContract) entry.getValue().getServiceContract();
            addFunctions(entry.getKey(), reference, referenceFunctions);
            ComponentReference compReference = definition.getReferences().get(referenceName);
            if (compReference != null && compReference.getServiceContract() != null) { //override the ServiceContract with a more specific type
                entry.getValue().setServiceContract(compReference.getServiceContract());
            } else {//not explicitly set, obtain the reference to the service from the composite if available
                ServiceContract contract = references.get(referenceName);
                if (contract != null) {
                    entry.getValue().setServiceContract(contract);
                } else {
                    //System.out.println("Warning, unable to refine contract " + entry.getValue().getName());
                }
            }
        }

        physical.setServiceFunctions(serviceFunctions);
        physical.setReferenceFunctions(referenceFunctions);
        physical.setCallbackFunctions(callbackFunctions);
    }

    private void processPropertyValues(LogicalComponent<XQueryImplementation> component, XQueryComponentDefinition physical) {
        for (Map.Entry<String, Document> entry : component.getPropertyValues().entrySet()) {
            String name = entry.getKey();
            Document value = entry.getValue();
            if (value != null) {
                physical.setPropertyValue(name, value);
            }
        }

        Map<String, QName> properties = new HashMap<String, QName>();
        ComponentDefinition<XQueryImplementation> definition = component.getDefinition();
        for (Map.Entry<String, Property> entry : definition.getImplementation().getComponentType().getProperties().entrySet()) {
            if (entry.getValue() instanceof XQueryProperty) {
                XQueryProperty property = (XQueryProperty) entry.getValue();
                properties.put(entry.getKey(), property.getVariableName());
            }
        }
        physical.setProperties(properties);
    }

    private void addFunctions(String name, XQueryServiceContract contract, Map<String, List<QName>> mappings) {
        List<QName> functions = new ArrayList<QName>();
        mappings.put(name, functions);
        for (Operation o : (List<Operation>) contract.getOperations()) {
            QName functionName = new QName(contract.getQname().getNamespaceURI(), o.getName(), contract.getQname().getPrefix());
            functions.add(functionName);
        }
    }

    public XQueryComponentWireSourceDefinition generateWireSource(LogicalComponent<XQueryImplementation> source,
                                                                  LogicalReference reference,
                                                                  Policy policy)
            throws GenerationException {

        XQueryComponentWireSourceDefinition sourceDefinition = new XQueryComponentWireSourceDefinition();
        sourceDefinition.setUri(reference.getUri());
        if (reference.getDefinition().getServiceContract().isConversational()) {
            sourceDefinition.setInteractionType(InteractionType.CONVERSATIONAL);
        }
        return sourceDefinition;
    }

    public PhysicalWireSourceDefinition generateCallbackWireSource(LogicalComponent<XQueryImplementation> source,
                                                                   ServiceContract<?> serviceContract,
                                                                   Policy policy) throws GenerationException {
        XQueryComponentWireSourceDefinition sourceDefinition = new XQueryComponentWireSourceDefinition();
        XQueryComponentType type = source.getDefinition().getImplementation().getComponentType();
        String name = null;
        for (Map.Entry<String, ServiceDefinition> entry : type.getServices().entrySet()) {
            if (entry.getValue().getServiceContract().isAssignableFrom(serviceContract)) {
                name = entry.getKey();
                break;
            }
        }
        if (name == null) {
            String interfaze = serviceContract.getQualifiedInterfaceName();
            throw new GenerationException("Callback  not found for type: " + interfaze, interfaze);
        }
        sourceDefinition.setUri(URI.create(source.getUri().toString() + "#" + name));
        sourceDefinition.setOptimizable(false);
        return sourceDefinition;
    }

    public PhysicalWireTargetDefinition generateWireTarget(LogicalService service, LogicalComponent<XQueryImplementation> component, Policy policy)
            throws GenerationException {
        XQueryComponentWireTargetDefinition targetDefinition = new XQueryComponentWireTargetDefinition();
        targetDefinition.setUri(service.getUri());
        return targetDefinition;
    }

    public PhysicalWireSourceDefinition generateResourceWireSource(LogicalComponent<XQueryImplementation> source, LogicalResource<?> resource)
            throws GenerationException {
        XQueryComponentWireSourceDefinition sourceDefinition = new XQueryComponentWireSourceDefinition();
        sourceDefinition.setUri(source.getUri());
        return sourceDefinition;
    }
}
