/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
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
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;

import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaType;
import org.fabric3.idl.wsdl.version.WsdlVersionChecker.WsdlVersion;
import org.fabric3.spi.model.type.DataType;
import org.fabric3.spi.model.type.Operation;

/**
 * WSDL 1.1 processor implementation.
 * 
 * @version $Revsion$ $Date$
 */
public class Wsdl11Processor extends AbstractWsdlProcessor implements WsdlProcessor {
    
    /**
     * @param wsdlProcessorRegistry Injected default processor.
     */
    public Wsdl11Processor(WsdlProcessorRegistry wsdlProcessorRegistry) {
        wsdlProcessorRegistry.registerProcessor(WsdlVersion.VERSION_1_1, this);
    }

    /**
     * @see @see org.fabric3.idl.wsdl.processor.WsdlProcessor#getOperations(javax.xml.namespace.QName, java.net.URL)
     */
    public List<Operation<?>> getOperations(QName portTypeOrInterfaceName, URL wsdlUrl) {
        
        try {
            
            List<Operation<?>> operations = new LinkedList<Operation<?>>();
            
            WSDLReader reader = WSDLFactory.newInstance().newWSDLReader();
            
            Definition definition = reader.readWSDL(wsdlUrl.toExternalForm());
            PortType portType = definition.getPortType(portTypeOrInterfaceName);
            
            if(portType == null) {
                throw new WsdlProcessorException("Port type not found " + portTypeOrInterfaceName);
            }
            
            List<XmlSchema> xmlSchemas = getXmlSchemas(definition);
            
            for(Object obj : portType.getOperations()) {                
                Operation<XmlSchemaType> op = getOperation(xmlSchemas, obj);                
                operations.add(op);                
            }
            
            return operations;
            
        } catch (WSDLException ex) {
            throw new WsdlProcessorException("Unable to parse WSDL " + wsdlUrl, ex);
        }
        
    }

    /*
     * Creates an F3 operation from a WSDL operation.
     */
    private Operation<XmlSchemaType> getOperation(List<XmlSchema> xmlSchemas, Object obj) {
        
        javax.wsdl.Operation operation = (javax.wsdl.Operation) obj;
        
        Input input = operation.getInput();
        Output output = operation.getOutput();
        Map faults = operation.getFaults();
        
        String name = operation.getName();
        DataType<List<DataType<XmlSchemaType>>> inputType = getInputType(input, xmlSchemas);
        DataType<XmlSchemaType> outputType = getOutputType(output, xmlSchemas);
        List<DataType<XmlSchemaType>> faultTypes = getFaultTypes(faults, xmlSchemas);
        
        return new Operation<XmlSchemaType>(name, inputType, outputType, faultTypes);
        
    }
    
    /*
     * Gets the fault types.
     */
    @SuppressWarnings("unchecked")
    private List<DataType<XmlSchemaType>> getFaultTypes(Map faults, List<XmlSchema> xmlSchemas) {
        
        List<DataType<XmlSchemaType>> types = new LinkedList<DataType<XmlSchemaType>>();
        
        for(Fault fault : (Collection<Fault>) faults.values()) {
            
            Part part = (Part) fault.getMessage().getOrderedParts(null).get(0);  
            DataType<XmlSchemaType> dataType = getDataType(part.getElementName(), xmlSchemas);
            if(dataType != null) {
                types.add(dataType);
            }        
            
        }
        
        return types;
        
    }

    /*
     * Get the output type.
     */
    private DataType<XmlSchemaType> getOutputType(Output output, List<XmlSchema> xmlSchemas) {
        
        if(output == null) return null;
        
        Message message = output.getMessage();
        Part part = (Part) message.getOrderedParts(null).get(0);
        
        return getDataType(part.getElementName(), xmlSchemas);
        
    }

    /*
     * Get the input type.
     */
    @SuppressWarnings("unchecked")
    private DataType<List<DataType<XmlSchemaType>>> getInputType(Input input, List<XmlSchema> xmlSchemas) {
        
        if(input == null) return null;
        
        Message message = input.getMessage();
        List<Part> parts = message.getOrderedParts(null);
        
        List<DataType<XmlSchemaType>> types = new LinkedList<DataType<XmlSchemaType>>();
        
        for(Part part : parts) {    
            DataType<XmlSchemaType> dataType = getDataType(part.getElementName(), xmlSchemas);
            if(dataType != null) {
                types.add(dataType);
            }        
        }
        
        return new DataType<List<DataType<XmlSchemaType>>>(Object.class, types);
        
    }

    /*
     * Get all the inline schemas.
     */
    private List<XmlSchema> getXmlSchemas(Definition definition) {
        
        List<XmlSchema> xmlSchemas = new LinkedList<XmlSchema>();
        
        Types types = definition.getTypes();
        for(Object obj : types.getExtensibilityElements()) {
            if(obj instanceof XmlSchema) {
                xmlSchemas.add((XmlSchema) obj);
            }
        }
        
        return xmlSchemas;
        
    }

}
