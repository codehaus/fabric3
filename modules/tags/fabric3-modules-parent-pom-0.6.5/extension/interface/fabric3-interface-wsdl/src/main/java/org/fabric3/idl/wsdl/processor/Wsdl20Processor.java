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
import java.util.LinkedList;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.woden.WSDLException;
import org.apache.woden.WSDLFactory;
import org.apache.woden.WSDLReader;
import org.apache.woden.schema.Schema;
import org.apache.woden.wsdl20.Description;
import org.apache.woden.wsdl20.ElementDeclaration;
import org.apache.woden.wsdl20.Interface;
import org.apache.woden.wsdl20.InterfaceFaultReference;
import org.apache.woden.wsdl20.InterfaceMessageReference;
import org.apache.woden.wsdl20.InterfaceOperation;
import org.apache.woden.wsdl20.enumeration.Direction;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchemaType;
import org.fabric3.idl.wsdl.version.WsdlVersionChecker.WsdlVersion;
import org.fabric3.scdl.DataType;
import org.fabric3.scdl.Operation;
import org.w3c.dom.Document;

/**
 * WSDL 2.0 processor implementation.
 * 
 * @version $Revsion$ $Date$
 */
public class Wsdl20Processor extends AbstractWsdlProcessor implements WsdlProcessor {
    
    /**
     * @param wsdlProcessorRegistry Injected default processor.
     */
    public Wsdl20Processor(WsdlProcessorRegistry wsdlProcessorRegistry) {
        wsdlProcessorRegistry.registerProcessor(WsdlVersion.VERSION_2_0, this);
    }

    public List<Operation<XmlSchemaType>> getOperations(QName portTypeOrInterfaceName, URL wsdlUrl) {
        
        try {
            
            List<Operation<XmlSchemaType>> operations = new LinkedList<Operation<XmlSchemaType>>();
            
            WSDLReader reader = WSDLFactory.newInstance().newWSDLReader();
            
            Description description = reader.readWSDL(wsdlUrl.toExternalForm());
            Interface interfaze = description.getInterface(portTypeOrInterfaceName);
            
            if(interfaze == null) {
                throw new WsdlProcessorException("Interface not found " + portTypeOrInterfaceName);
            }
            
            XmlSchemaCollection xmlSchema = getXmlSchema(description);
            
            for(InterfaceOperation operation : interfaze.getAllInterfaceOperations()) {
                Operation<XmlSchemaType> op = getOperation(xmlSchema, operation);                
                operations.add(op);                
            }
            
            return operations;
            
        } catch (WSDLException ex) {
            throw new WsdlProcessorException("Unable to parse WSDL " + wsdlUrl, ex);
        }
        
    }

    /*
     * Creates F3 operation from WSDL 2.0 operation.
     */
    private Operation<XmlSchemaType> getOperation(XmlSchemaCollection xmlSchema, InterfaceOperation operation) {
        
        String name = operation.getName().getLocalPart();
        
        InterfaceMessageReference[] messageReferences = operation.getInterfaceMessageReferences();
        
        List<DataType<XmlSchemaType>> faultTypes = getFaultTypes(xmlSchema, operation);
        
        List<DataType<XmlSchemaType>> inputTypes = new LinkedList<DataType<XmlSchemaType>>();
        DataType<XmlSchemaType> outputType = null;
        
        for(InterfaceMessageReference messageReference : messageReferences) {
            ElementDeclaration elementDeclaration = messageReference.getElementDeclaration();
            QName qName = elementDeclaration.getName();
            DataType<XmlSchemaType> dataType = getDataType(qName, xmlSchema);
            if(dataType != null) {
                if(messageReference.getDirection().equals(Direction.IN)) {
                    inputTypes.add(dataType);
                } else if(messageReference.getDirection().equals(Direction.OUT)) {
                    if(outputType != null) {
                        throw new WsdlProcessorException("Multipart output is not supported");
                    }
                    outputType = dataType;
                }
                // TODO What about in out?
            }
        }
        
        DataType<List<DataType<XmlSchemaType>>> inputType = new DataType<List<DataType<XmlSchemaType>>>(Object.class, inputTypes);
        
        return new Operation<XmlSchemaType>(name, inputType, outputType, faultTypes);
    }

    /*
     * Gets the fault types.
     */
    private List<DataType<XmlSchemaType>> getFaultTypes(XmlSchemaCollection xmlSchema, InterfaceOperation operation) {
        
        InterfaceFaultReference[] faultReferences = operation.getInterfaceFaultReferences();
        List<DataType<XmlSchemaType>> faultTypes = new LinkedList<DataType<XmlSchemaType>>();
        for(InterfaceFaultReference faultReference : faultReferences) {
            ElementDeclaration elementDeclaration = faultReference.getInterfaceFault().getElementDeclaration();
            QName qName = elementDeclaration.getName();
            DataType<XmlSchemaType> dataType = getDataType(qName, xmlSchema);
            if(dataType != null) {
                faultTypes.add(dataType);
            }
        }
        return faultTypes;
        
    }

    /*
     * Get all the inline schemas.
     */
    private XmlSchemaCollection getXmlSchema(Description description) {
        
        XmlSchemaCollection collection = new XmlSchemaCollection();
        
        Schema[] schemas = description.toElement().getTypesElement().getSchemas();
        for(Schema schema : schemas) {
            for(Document doc : schema.getSchemaDefinition().getAllSchemas()) {
                collection.read(doc.getDocumentElement());
            }
        }
        
        return collection;
        
    }

}
