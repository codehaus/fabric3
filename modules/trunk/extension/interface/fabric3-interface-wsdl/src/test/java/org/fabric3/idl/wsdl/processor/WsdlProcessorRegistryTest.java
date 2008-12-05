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
package org.fabric3.idl.wsdl.processor;

import java.net.URL;
import java.util.List;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.ws.commons.schema.XmlSchemaType;
import org.fabric3.idl.wsdl.version.DefaultWsdlVersionChecker;
import org.fabric3.model.type.service.DataType;
import org.fabric3.model.type.service.Operation;

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
