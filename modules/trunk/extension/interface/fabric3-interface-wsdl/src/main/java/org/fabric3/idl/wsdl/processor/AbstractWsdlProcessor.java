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

import javax.xml.namespace.QName;

import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaType;
import org.fabric3.scdl.DataType;

/**
 * Super class for WSDL processors.
 * 
 * @version $Revision$ $Date$
 *
 */
public abstract class AbstractWsdlProcessor {
    
    /*
     * Create a data type with the XML type for the part.
     */
    protected DataType<XmlSchemaType> getDataType(QName qName, XmlSchemaCollection xmlSchema) {

        XmlSchemaType type = xmlSchema.getTypeByQName(qName);
        if(type != null) {
            return new DataType<XmlSchemaType>(Object.class, type);
        } else {
            XmlSchemaElement element = xmlSchema.getElementByQName(qName);
            if(element != null) {
                return new DataType<XmlSchemaType>(Object.class, element.getSchemaType());
            }
        }
        throw new WsdlProcessorException("Unable to find type " + qName);
        
    }

}
