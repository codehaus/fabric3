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
import org.osoa.sca.annotations.Reference;
import org.w3c.dom.Document;

import org.fabric3.idl.wsdl.version.WsdlVersionChecker.WsdlVersion;
import org.fabric3.model.type.service.DataType;
import org.fabric3.model.type.service.Operation;

/**
 * WSDL 2.0 processor implementation.
 *
 * @version $Revsion$ $Date$
 */
public class Wsdl20Processor extends AbstractWsdlProcessor implements WsdlProcessor {

    /**
     * @param wsdlProcessorRegistry Injected default processor.
     */
    public Wsdl20Processor(@Reference(name = "registry") WsdlProcessorRegistry wsdlProcessorRegistry) {
        wsdlProcessorRegistry.registerProcessor(WsdlVersion.VERSION_2_0, this);
    }

    public List<Operation<XmlSchemaType>> getOperations(QName portTypeOrInterfaceName, URL wsdlUrl) {

        try {

            List<Operation<XmlSchemaType>> operations = new LinkedList<Operation<XmlSchemaType>>();

            WSDLReader reader = WSDLFactory.newInstance().newWSDLReader();

            Description description = reader.readWSDL(wsdlUrl.toExternalForm());
            Interface interfaze = description.getInterface(portTypeOrInterfaceName);

            if (interfaze == null) {
                throw new WsdlProcessorException("Interface not found " + portTypeOrInterfaceName);
            }

            XmlSchemaCollection xmlSchema = getXmlSchema(description);

            for (InterfaceOperation operation : interfaze.getAllInterfaceOperations()) {
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

        for (InterfaceMessageReference messageReference : messageReferences) {
            ElementDeclaration elementDeclaration = messageReference.getElementDeclaration();
            QName qName = elementDeclaration.getName();
            DataType<XmlSchemaType> dataType = getDataType(qName, xmlSchema);
            if (dataType != null) {
                if (messageReference.getDirection().equals(Direction.IN)) {
                    inputTypes.add(dataType);
                } else if (messageReference.getDirection().equals(Direction.OUT)) {
                    if (outputType != null) {
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
        for (InterfaceFaultReference faultReference : faultReferences) {
            ElementDeclaration elementDeclaration = faultReference.getInterfaceFault().getElementDeclaration();
            QName qName = elementDeclaration.getName();
            DataType<XmlSchemaType> dataType = getDataType(qName, xmlSchema);
            if (dataType != null) {
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
        for (Schema schema : schemas) {
            for (Document doc : schema.getSchemaDefinition().getAllSchemas()) {
                collection.read(doc.getDocumentElement());
            }
        }

        return collection;

    }

}
