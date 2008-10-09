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
import org.fabric3.idl.wsdl.version.WsdlVersionChecker.WsdlVersion;
import org.fabric3.scdl.DataType;
import org.fabric3.scdl.Operation;

import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Service;
import org.w3c.dom.Element;

/**
 * WSDL 1.1 processor implementation.
 * 
 * @version $Revsion$ $Date$
 */
@Service(interfaces={Wsdl11Processor.class,WsdlProcessor.class})
public class Wsdl11Processor extends AbstractWsdlProcessor implements WsdlProcessor {
    
    /**
     * @param wsdlProcessorRegistry Injected default processor.
     */
    public Wsdl11Processor(@Reference(name="registry") WsdlProcessorRegistry wsdlProcessorRegistry) {
        wsdlProcessorRegistry.registerProcessor(WsdlVersion.VERSION_1_1, this);
    }

    public List<Operation<XmlSchemaType>> getOperations(QName portTypeOrInterfaceName, URL wsdlUrl) {
        
        try {
            
            List<Operation<XmlSchemaType>> operations = new LinkedList<Operation<XmlSchemaType>>();
            
            WSDLFactory factory = WSDLFactory.newInstance();
            WSDLReader reader = factory.newWSDLReader();
            reader.setExtensionRegistry(factory.newPopulatedExtensionRegistry());
            
            Definition definition = reader.readWSDL(wsdlUrl.toExternalForm());            
            PortType portType = definition.getPortType(portTypeOrInterfaceName);
            
            if(portType == null) {
                throw new WsdlProcessorException("Port type not found " + portTypeOrInterfaceName);
            }
            
            XmlSchemaCollection xmlSchema = getXmlSchema(definition);
            
            for(Object obj : portType.getOperations()) {                
                Operation<XmlSchemaType> op = getOperation(xmlSchema, obj);                
                operations.add(op);                
            }
            
            return operations;
            
        } catch (WSDLException ex) {
            throw new WsdlProcessorException("Unable to parse WSDL " + wsdlUrl, ex);
        }
        
    }

     public List<Operation<XmlSchemaType>> getOperations(PortType portType,XmlSchemaCollection xmlSchema) {
        
            List<Operation<XmlSchemaType>> operations = new LinkedList<Operation<XmlSchemaType>>();
            
            
            if(portType == null || xmlSchema==null) {
                throw new WsdlProcessorException("Port type is null ");
            }
            
            for(Object obj : portType.getOperations()) {                
                Operation<XmlSchemaType> op = getOperation(xmlSchema, obj);                
                operations.add(op);                
            }
            
            return operations;
    }
    /*
     * Creates an F3 operation from a WSDL operation.
     */
    private Operation<XmlSchemaType> getOperation(XmlSchemaCollection xmlSchema, Object obj) {
        
        javax.wsdl.Operation operation = (javax.wsdl.Operation) obj;
        
        Input input = operation.getInput();
        Output output = operation.getOutput();
        Map faults = operation.getFaults();
        
        String name = operation.getName();
        DataType<List<DataType<XmlSchemaType>>> inputType = getInputType(input, xmlSchema);
        DataType<XmlSchemaType> outputType = getOutputType(output, xmlSchema);
        List<DataType<XmlSchemaType>> faultTypes = getFaultTypes(faults, xmlSchema);
        
        return new Operation<XmlSchemaType>(name, inputType, outputType, faultTypes);
        
    }
    
    /*
     * Gets the fault types.
     */
    @SuppressWarnings("unchecked")
    private List<DataType<XmlSchemaType>> getFaultTypes(Map faults, XmlSchemaCollection xmlSchema) {
        
        List<DataType<XmlSchemaType>> types = new LinkedList<DataType<XmlSchemaType>>();
        
        for(Fault fault : (Collection<Fault>) faults.values()) {
            
            Part part = (Part) fault.getMessage().getOrderedParts(null).get(0);  
            DataType<XmlSchemaType> dataType = getDataType(part.getElementName(), xmlSchema);
            if(dataType != null) {
                types.add(dataType);
            }        
            
        }
        
        return types;
        
    }

    /*
     * Get the output type.
     */
    private DataType<XmlSchemaType> getOutputType(Output output, XmlSchemaCollection xmlSchema) {
        
        if(output == null) return null;
        
        Message message = output.getMessage();
        Part part = (Part) message.getOrderedParts(null).get(0);
        
        return getDataType(part.getElementName(), xmlSchema);
        
    }

    /*
     * Get the input type.
     */
    @SuppressWarnings("unchecked")
    private DataType<List<DataType<XmlSchemaType>>> getInputType(Input input, XmlSchemaCollection xmlSchema) {
        
        if(input == null) return null;
        
        Message message = input.getMessage();
        List<Part> parts = message.getOrderedParts(null);
        
        List<DataType<XmlSchemaType>> types = new LinkedList<DataType<XmlSchemaType>>();
        
        for(Part part : parts) {    
            DataType<XmlSchemaType> dataType = getDataType(part.getElementName(), xmlSchema);
            if(dataType != null) {
                types.add(dataType);
            }        
        }
        
        return new DataType<List<DataType<XmlSchemaType>>>(Object.class, types);
        
    }

    /*
     * Get all the inline schemas.
     */
    private XmlSchemaCollection getXmlSchema(Definition definition) {
        
        XmlSchemaCollection collection = new XmlSchemaCollection();
        Types types = definition.getTypes();
        for(Object obj : types.getExtensibilityElements()) {
            if(obj instanceof Schema) {
                Schema schema = (Schema) obj;
                Element element = schema.getElement();
                collection.setBaseUri(schema.getDocumentBaseURI());
                collection.read(element);
            }
        }
        return collection;
        
    }

}
