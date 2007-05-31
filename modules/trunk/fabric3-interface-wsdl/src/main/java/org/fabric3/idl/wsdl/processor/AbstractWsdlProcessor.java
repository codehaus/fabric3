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

import javax.xml.namespace.QName;

import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaType;
import org.fabric3.spi.model.type.DataType;

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
    protected DataType<XmlSchemaType> getDataType(QName qName, XmlSchema xmlSchema) {

        XmlSchemaType type = xmlSchema.getTypeByName(qName);
        if(type != null) {
            return new DataType<XmlSchemaType>(Object.class, type);
        } else {
            XmlSchemaElement element = xmlSchema.getElementByName(qName);
            if(element != null) {
                return new DataType<XmlSchemaType>(Object.class, element.getSchemaType());
            }
        }
        throw new WsdlProcessorException("Unable to find type " + qName);
        
    }

}
