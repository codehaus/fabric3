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
package org.fabric3.fabric.marshaller;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.fabric3.spi.marshaller.MarshalException;
import org.fabric3.spi.marshaller.Marshaller;
import org.fabric3.spi.marshaller.MarshallerRegistry;

/**
 * Default implementation of the marshaller registry.
 * 
 * @version $Revision$ $Date$
 */
public class DefaultMarshallerRegistry implements MarshallerRegistry {
    
    /**
     * Marshaller cache.
     */ 
    private Map<Class<?>, Marshaller> marshallerCache = new HashMap<Class<?>, Marshaller>();
    
    /**
     * Unmarshaller cache.
     */ 
    private Map<QName, Marshaller> unmarshallerCache = new HashMap<QName, Marshaller>();

    /**
     * Registers a model object marshaller.
     * 
     * @param modelClass Model obejct class.
     * @param xmlName Qualified name of the root element of the marshalled XML.
     * @param marshaller Model object marshaller.
     */
    public void registerMarshaller(Class<?> modelClass, QName xmlName, Marshaller marshaller) {
        
        marshallerCache.put(modelClass, marshaller);
        unmarshallerCache.put(xmlName, marshaller);

    }
    
    /**
     * Marshalls a model object.
     * 
     * @param modelObject Model object to be marshalled.
     * @param writer Writer to which marshalled information is written.
     */
    public void marshall(Object modelObject, XMLStreamWriter writer) throws MarshalException {

        Class<?> modelClass = modelObject.getClass();
        Marshaller marshaller = getMarshaller(modelClass);
        
        try {
            writer.writeStartDocument();
            marshaller.marshal(modelObject, writer);  
            writer.writeEndDocument();  
        } catch (XMLStreamException ex) {
            throw new MarshalException(ex);
        }    

    }
    
    /**
     * Unmarshalls an XML stream to a model object.
     * 
     * @param reader Reader from which marshalled information is read.
     * @return Model object from the marshalled stream.
     */
    public Object unmarshall(XMLStreamReader reader) throws MarshalException {
        
        try {
            
            while(reader.next() != XMLStreamConstants.START_ELEMENT) {
            }
            
            QName xmlName = reader.getName();
            Marshaller marshaller = getMarshaller(xmlName);

            return marshaller.unmarshal(reader);
            
        } catch (XMLStreamException ex) {
            throw new MarshalException(ex);
        }

    }
    
    /**
     * Gets the marshaller capable of marshalling the specified model object class.
     * @param modelClass Model object class.
     * @return Marshaller for marshalling the model object.
     * @throws MarshalException If the marshaller is not found.
     */
    public Marshaller getMarshaller(Class<?> modelClass) throws MarshalException {
        
        Marshaller marshaller = marshallerCache.get(modelClass);
        
        if(marshaller == null) {
            throw new MarshalException("No marshaller for " + modelClass);
        }
        return marshaller;
        
    }
    
    /**
     * Gets the marshaller capable of unmarshalling the XML element.
     * @param xmlName Qualified naem of the XML element.
     * @return Marshaller for unmarshalling the XML element.
     * @throws MarshalException If the marshaller is not found.
     */
    public Marshaller getMarshaller(QName xmlName) throws MarshalException {
        
        Marshaller marshaller = unmarshallerCache.get(xmlName);
        
        if(marshaller == null) {
            throw new MarshalException("No marshaller for " + xmlName);
        }
        return marshaller;
        
    }

}
