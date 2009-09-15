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
package org.fabric3.idl.wsdl.processor;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.wsdl.Definition;
import javax.wsdl.Fault;
import javax.wsdl.Input;
import javax.wsdl.Message;
import javax.wsdl.Output;
import javax.wsdl.Part;
import javax.wsdl.PortType;
import javax.xml.namespace.QName;

import org.osoa.sca.annotations.Reference;

import org.fabric3.idl.wsdl.model.WsdlServiceContract;
import org.fabric3.model.type.service.DataType;
import org.fabric3.model.type.service.Operation;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.ResourceElement;
import org.fabric3.spi.contribution.manifest.QNameSymbol;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.model.type.XSDType;

/**
 * WSDL 1.1 processor implementation.
 *
 * @version $Revsion$ $Date$
 */
public class Wsdl11ContractProcessor implements WsdlContractProcessor {
    private MetaDataStore store;

    public Wsdl11ContractProcessor(@Reference MetaDataStore store) {
        this.store = store;
    }

    public WsdlServiceContract introspect(QName portTypeName, IntrospectionContext context) {
        WsdlServiceContract contract = new WsdlServiceContract();
        contract.setQname(portTypeName);

        URI contributionUri = context.getContributionUri();
        PortType portType = resolvePortType(null, portTypeName, contributionUri);
        if (portType == null) {
            PortTypeNotFound error = new PortTypeNotFound("Port type not found " + portType);
            context.addError(error);
            return contract;
        }
        List<Operation> operations = new LinkedList<Operation>();
        for (Object wsdlOperation : portType.getOperations()) {
            Operation op = createOperation((javax.wsdl.Operation) wsdlOperation);
            operations.add(op);
        }
        contract.setOperations(operations);
        return contract;

    }

    public List<Operation> getOperations(PortType portType) {
        List<Operation> operations = new LinkedList<Operation>();
        for (Object obj : portType.getOperations()) {
            Operation op = createOperation((javax.wsdl.Operation) obj);
            operations.add(op);
        }
        return operations;
    }

    /**
     * Creates a operation model object from a WSDL operation.
     *
     * @param operation the WSDL operation
     * @return the operation model object
     */
    private Operation createOperation(javax.wsdl.Operation operation) {
        Input input = operation.getInput();
        List<DataType<?>> inputTypes = getInputTypes(input.getMessage());

        Map faults = operation.getFaults();
        List<DataType<?>> faultTypes = getFaultTypes(faults);

        Output output = operation.getOutput();
        DataType<?> outputType = getOutputType(output);

        String name = operation.getName();
        return new Operation(name, inputTypes, outputType, faultTypes);
    }

    @SuppressWarnings({"unchecked"})
    private List<DataType<?>> getInputTypes(Message message) {
        List<DataType<?>> types = new ArrayList<DataType<?>>();
        for (Part part : (Collection<Part>) message.getParts().values()) {
            XSDType dataType = getDataType(part.getElementName());
            if (dataType != null) {
                types.add(dataType);
            }
        }
        return types;
    }

    @SuppressWarnings("unchecked")
    private List<DataType<?>> getFaultTypes(Map faults) {
        List<DataType<?>> types = new LinkedList<DataType<?>>();
        for (Fault fault : (Collection<Fault>) faults.values()) {
            Part part = (Part) fault.getMessage().getOrderedParts(null).get(0);
            XSDType dataType = getDataType(part.getElementName());
            if (dataType != null) {
                types.add(dataType);
            }
        }
        return types;

    }

    private DataType<?> getOutputType(Output output) {
        if (output == null) {
            return null;
        }
        Message message = output.getMessage();
        Part part = (Part) message.getOrderedParts(null).get(0);
        return getDataType(part.getElementName());
    }

    private XSDType getDataType(QName elementName) {
        // TODO create XSDType from schema type
        return null;
    }


    @SuppressWarnings({"unchecked"})
    private PortType resolvePortType(String namespace, QName portName, URI contributionUri) {
        List<Resource> resources = store.resolveResources(contributionUri);

        for (Resource resource : resources) {
            if ("text/wsdl+xml".equals(resource.getContentType())) {
                // resource type is a WSDL
                ResourceElement<QNameSymbol, Definition> element = (ResourceElement<QNameSymbol, Definition>) resource.getResourceElements().get(0);
                if (namespace.equals(element.getSymbol().getKey().getNamespaceURI())) {
                    Definition model = element.getValue();
                    return model.getPortType(portName);
                }
            }
        }
        return null;
    }

}
