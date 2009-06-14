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
