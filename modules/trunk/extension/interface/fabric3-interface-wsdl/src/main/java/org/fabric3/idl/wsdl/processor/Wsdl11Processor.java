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

import java.net.URL;
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
import javax.wsdl.Types;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;

import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchemaType;
import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Service;
import org.w3c.dom.Element;

import org.fabric3.idl.wsdl.version.WsdlVersionChecker.WsdlVersion;
import org.fabric3.model.type.service.DataType;
import org.fabric3.model.type.service.Operation;

/**
 * WSDL 1.1 processor implementation.
 *
 * @version $Revsion$ $Date$
 */
@Service(interfaces = {Wsdl11Processor.class, WsdlProcessor.class})
public class Wsdl11Processor extends AbstractWsdlProcessor implements WsdlProcessor {

    /**
     * @param wsdlProcessorRegistry Injected default processor.
     */
    public Wsdl11Processor(@Reference(name = "registry") WsdlProcessorRegistry wsdlProcessorRegistry) {
        wsdlProcessorRegistry.registerProcessor(WsdlVersion.VERSION_1_1, this);
    }

    public List<Operation> getOperations(QName portTypeOrInterfaceName, URL wsdlUrl) {

        try {

            List<Operation> operations = new LinkedList<Operation>();

            WSDLFactory factory = WSDLFactory.newInstance();
            WSDLReader reader = factory.newWSDLReader();
            reader.setExtensionRegistry(factory.newPopulatedExtensionRegistry());

            Definition definition = reader.readWSDL(wsdlUrl.toExternalForm());
            PortType portType = definition.getPortType(portTypeOrInterfaceName);

            if (portType == null) {
                throw new WsdlProcessorException("Port type not found " + portTypeOrInterfaceName);
            }

            XmlSchemaCollection xmlSchema = getXmlSchema(definition);

            for (Object obj : portType.getOperations()) {
                Operation op = getOperation(xmlSchema, obj);
                operations.add(op);
            }

            return operations;

        } catch (WSDLException ex) {
            throw new WsdlProcessorException("Unable to parse WSDL " + wsdlUrl, ex);
        }

    }

    public List<Operation> getOperations(PortType portType, XmlSchemaCollection xmlSchema) {

        List<Operation> operations = new LinkedList<Operation>();


        if (portType == null || xmlSchema == null) {
            throw new WsdlProcessorException("Port type is null ");
        }

        for (Object obj : portType.getOperations()) {
            Operation op = getOperation(xmlSchema, obj);
            operations.add(op);
        }

        return operations;
    }

    /*
    * Creates an F3 operation from a WSDL operation.
    */
    private Operation getOperation(XmlSchemaCollection xmlSchema, Object obj) {

        javax.wsdl.Operation operation = (javax.wsdl.Operation) obj;

        Input input = operation.getInput();
        Output output = operation.getOutput();
        Map faults = operation.getFaults();

        String name = operation.getName();
        DataType<List<DataType<?>>> inputType = getInputType(input, xmlSchema);
        DataType<?> outputType = getOutputType(output, xmlSchema);
        List<DataType<?>> faultTypes = getFaultTypes(faults, xmlSchema);

        return new Operation(name, inputType, outputType, faultTypes);

    }

    /*
    * Gets the fault types.
    */
    @SuppressWarnings("unchecked")
    private List<DataType<?>> getFaultTypes(Map faults, XmlSchemaCollection xmlSchema) {

        List<DataType<?>> types = new LinkedList<DataType<?>>();

        for (Fault fault : (Collection<Fault>) faults.values()) {

            Part part = (Part) fault.getMessage().getOrderedParts(null).get(0);
            DataType<XmlSchemaType> dataType = getDataType(part.getElementName(), xmlSchema);
            if (dataType != null) {
                types.add(dataType);
            }

        }

        return types;

    }

    /*
     * Get the output type.
     */
    private DataType<?> getOutputType(Output output, XmlSchemaCollection xmlSchema) {

        if (output == null) return null;

        Message message = output.getMessage();
        Part part = (Part) message.getOrderedParts(null).get(0);

        return getDataType(part.getElementName(), xmlSchema);

    }

    /*
     * Get the input type.
     */
    @SuppressWarnings("unchecked")
    private DataType<List<DataType<?>>> getInputType(Input input, XmlSchemaCollection xmlSchema) {

        if (input == null) return null;

        Message message = input.getMessage();
        List<Part> parts = message.getOrderedParts(null);

        List<DataType<?>> types = new LinkedList<DataType<?>>();

        for (Part part : parts) {
            DataType<XmlSchemaType> dataType = getDataType(part.getElementName(), xmlSchema);
            if (dataType != null) {
                types.add(dataType);
            }
        }

        return new DataType<List<DataType<?>>>(Object.class, types);

    }

    /*
     * Get all the inline schemas.
     */
    private XmlSchemaCollection getXmlSchema(Definition definition) {

        XmlSchemaCollection collection = new XmlSchemaCollection();
        Types types = definition.getTypes();
        for (Object obj : types.getExtensibilityElements()) {
            if (obj instanceof Schema) {
                Schema schema = (Schema) obj;
                Element element = schema.getElement();
                collection.setBaseUri(schema.getDocumentBaseURI());
                collection.read(element);
            }
        }
        return collection;

    }

}
