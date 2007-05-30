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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.wsdl.Definition;
import javax.wsdl.Input;
import javax.wsdl.Output;
import javax.wsdl.PortType;
import javax.wsdl.WSDLException;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;

import org.fabric3.idl.wsdl.version.WsdlVersionChecker.WsdlVersion;
import org.fabric3.spi.model.type.Operation;

/**
 * WSDL 1.1 processor implementation.
 * 
 * @version $Revsion$ $Date$
 */
public class Wsdl11Processor implements WsdlProcessor {
    
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
            
            List<Operation<?>> oepartions = new LinkedList<Operation<?>>();
            
            WSDLFactory wsdlFactory = WSDLFactory.newInstance();
            WSDLReader reader = wsdlFactory.newWSDLReader();
            
            Definition definition = reader.readWSDL(wsdlUrl.toExternalForm());
            PortType portType = definition.getPortType(portTypeOrInterfaceName);
            
            if(portType == null) {
                throw new WsdlProcessorException("Port type not found " + portTypeOrInterfaceName);
            }
            
            for(Object op : portType.getOperations()) {
                javax.wsdl.Operation operation = (javax.wsdl.Operation) op;
                Input input = operation.getInput();
                Output output = operation.getOutput();
                Map faults = operation.getFaults();
                // TODO Populate the f3 operation
            }
            
            return oepartions;
            
        } catch (WSDLException ex) {
            throw new WsdlProcessorException("Unable to parse WSDL " + wsdlUrl, ex);
        }
    }

}
