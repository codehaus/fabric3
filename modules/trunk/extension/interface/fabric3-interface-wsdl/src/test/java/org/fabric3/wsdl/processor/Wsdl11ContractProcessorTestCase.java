package org.fabric3.wsdl.processor;

import javax.wsdl.Definition;
import javax.wsdl.PortType;
import javax.wsdl.Types;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;

import junit.framework.TestCase;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.w3c.dom.Element;

import org.fabric3.model.type.service.DataType;
import org.fabric3.model.type.service.Operation;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.model.type.XSDSimpleType;
import org.fabric3.wsdl.model.WsdlServiceContract;

/**
 * @version $Rev$ $Date$
 */
public class Wsdl11ContractProcessorTestCase extends TestCase {
    private static final QName STOCK_QUOTE_PORT_TYPE = new QName("http://example.com/stockquote.wsdl", "StockQuotePortType");
    private WsdlContractProcessor processor;
    private PortType portType;
    private XmlSchemaCollection schemaCollection;

    @SuppressWarnings({"unchecked"})
    public void testIntrospect() throws Exception {
        DefaultIntrospectionContext context = new DefaultIntrospectionContext();

        WsdlServiceContract contract = processor.introspect(portType, schemaCollection, context);

        assertEquals(1, contract.getOperations().size());
        Operation operation = contract.getOperations().get(0);
        assertEquals("GetLastTradePrice", operation.getName());
        assertEquals(1, operation.getInputTypes().size());

        DataType<QName> input = (DataType<QName>) operation.getInputTypes().get(0);
        assertTrue(input instanceof XSDSimpleType);
        assertEquals("string", input.getLogical().getLocalPart());

        assertEquals(0, operation.getFaultTypes().size());

        DataType<QName> output = (DataType<QName>) operation.getOutputType();
        assertTrue(output instanceof XSDSimpleType);
        assertEquals("float", output.getLogical().getLocalPart());
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        WSDLFactory factory = WSDLFactory.newInstance();
        WSDLReader reader = factory.newWSDLReader();
        reader.setFeature("javax.wsdl.verbose", false);
        reader.setExtensionRegistry(factory.newPopulatedExtensionRegistry());
        Definition definition = reader.readWSDL(getClass().getResource("example_1_1.wsdl").toURI().toString());
        portType = definition.getPortType(STOCK_QUOTE_PORT_TYPE);

        schemaCollection = parseSchema(definition);
        processor = new Wsdl11ContractProcessor();
    }

    private XmlSchemaCollection parseSchema(Definition definition) {
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
