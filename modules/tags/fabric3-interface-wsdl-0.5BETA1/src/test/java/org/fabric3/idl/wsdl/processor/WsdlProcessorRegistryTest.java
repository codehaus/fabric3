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
import java.util.List;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.ws.commons.schema.XmlSchemaType;
import org.fabric3.idl.wsdl.version.DefaultWsdlVersionChecker;
import org.fabric3.scdl.DataType;
import org.fabric3.scdl.Operation;

/**
 * @version $Revision$ $Date$
 */
public class WsdlProcessorRegistryTest extends TestCase {
    
    // Processor registry
    private WsdlProcessorRegistry processorRegistry = null;
    
    /**
     * Sets up the registry.
     */
    public void setUp() {
        
        processorRegistry = new WsdlProcessorRegistry(new DefaultWsdlVersionChecker());
        new Wsdl11Processor(processorRegistry);
        new Wsdl20Processor(processorRegistry);
        
    }

    /**
     * Checks for version 1.1
     */
    public void testGetVersion1_1() {    
        
        URL url = getClass().getClassLoader().getResource("example_1_1.wsdl");
        QName portTypeQName = new QName("http://example.com/stockquote.wsdl", "StockQuotePortType");
        
        List<Operation<XmlSchemaType>> operations = processorRegistry.getOperations(portTypeQName, url);
        assertEquals(1, operations.size());
        
        Operation<XmlSchemaType> operation = operations.get(0);
        assertEquals("GetLastTradePrice", operation.getName());
        
        DataType<List<DataType<XmlSchemaType>>> inputType = operation.getInputType();
        List<DataType<XmlSchemaType>> inputParts = inputType.getLogical();
        assertEquals(1, inputParts.size());
        
        DataType<XmlSchemaType> inputPart = inputParts.get(0);
        XmlSchemaType inputPartLogical = inputPart.getLogical();
        
        assertNotNull(inputPartLogical);
        assertEquals("string", inputPartLogical.getName());
        
        DataType<XmlSchemaType> outputType = operation.getOutputType();
        assertEquals("float", outputType.getLogical().getName());

    }

    /**
     * Checks for version 1.1
     */
    public void testGetVersion2_0() {    
        
        URL url = getClass().getClassLoader().getResource("example_2_0.wsdl");
        QName portTypeQName = new QName("http://greath.example.com/2004/wsdl/resSvc", "reservationInterface");
        
        List<Operation<XmlSchemaType>> operations = processorRegistry.getOperations(portTypeQName, url);
        assertEquals(1, operations.size());
        
        Operation<XmlSchemaType> operation = operations.get(0);
        assertEquals("opCheckAvailability", operation.getName());
        
        DataType<List<DataType<XmlSchemaType>>> inputType = operation.getInputType();
        List<DataType<XmlSchemaType>> inputParts = inputType.getLogical();
        assertEquals(1, inputParts.size());
        
        DataType<XmlSchemaType> inputPart = inputParts.get(0);
        XmlSchemaType inputPartLogical = inputPart.getLogical();
        
        assertNotNull(inputPartLogical);
        assertEquals("tCheckAvailability", inputPartLogical.getName());
        
        DataType<XmlSchemaType> outputType = operation.getOutputType();
        assertEquals("double", outputType.getLogical().getName());
        
    }

}
