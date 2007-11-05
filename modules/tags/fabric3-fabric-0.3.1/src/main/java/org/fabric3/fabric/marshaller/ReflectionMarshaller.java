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

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
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
import org.fabric3.scdl.ModelObject;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

/**
 * Reflection based implementation of the marshaller.
 * 
 * @version $Revision$ $Date$
 */
@EagerInit
public class ReflectionMarshaller implements Marshaller {
    
    /** Model object class. */
    private Class<?> modelClass;
    
    /** XML QName. */
    private QName xmlName;
    
    /** Property Descriptors. */
    private Map<String, PropertyDescriptor> propertyDescriptors = new HashMap<String, PropertyDescriptor>();
    
    /** Marshaller registry. */
    private MarshallerRegistry registry;
    
    /**
     * Sets the model class.
     * @param modelClassName Model class.
     * @throws MarshalException If unable to introspect properties.
     */
    @Property
    public void setModelClass(String modelClassName) throws MarshalException {

        try {
            modelClass = Class.forName(modelClassName);
        } catch (ClassNotFoundException e) {
            throw new MarshalException(e);
        }
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(modelClass);
            for(PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
                propertyDescriptors.put(pd.getName(), pd);
            }
        } catch(IntrospectionException ex) {
            throw new MarshalException(ex);
        }
        
    }
    
    /**
     * Sets the XML name. 
     * @param xmlName XML name.
     */
    @Property
    public void setXmlName(String xmlName) {
        this.xmlName = QName.valueOf(xmlName);
    }
    
    /**
     * Sets the marshaller registry. 
     * @param marshallerRegistry Marshaller registry.
     */
    @Reference
    public void setMarshallerRegistry(MarshallerRegistry registry) {
        this.registry = registry;
    }
    
    /**
     * Registers the marshaller with the registry.
     *
     */
    @Init
    public void init() {        
        if(xmlName != null && modelClass != null && registry != null) {
            registry.registerMarshaller(modelClass, xmlName, this);
        }
    }

    /**
     * Unmarshalls an XML stream to a model object.
     * 
     * @param reader XML stream from where the marshalled XML is read.
     * @return Physical component definition.
     * @throws MarshalException In case of any unmarshalling error.
     */
    @SuppressWarnings("unchecked")
    public Object unmarshal(XMLStreamReader reader) throws MarshalException {
        
        try {
            
            Object modelObject = modelClass.newInstance();
            while(true) {
                
                if(reader.next() == XMLStreamConstants.END_ELEMENT && reader.getName().equals(xmlName)) {
                    break;
                }
                
                String propName = reader.getName().getLocalPart();
                PropertyDescriptor pd = propertyDescriptors.get(propName);
                
                Class<?> propType = pd.getPropertyType();
                if(ModelObject.class.isAssignableFrom(propType)) {
                    
                    reader.next();
                    QName nestedXmlName = reader.getName();                    
                    Marshaller marshaller = registry.getMarshaller(nestedXmlName);
                    pd.getWriteMethod().invoke(modelObject, marshaller.unmarshal(reader));
                    
                } else if(Collection.class.isAssignableFrom(propType)) {
                    
                    Collection col = (Collection) pd.getReadMethod().invoke(modelObject);
                    while(reader.next() == XMLStreamConstants.START_ELEMENT) {
                        QName nestedXmlName = reader.getName();                        
                        Marshaller marshaller = registry.getMarshaller(nestedXmlName);
                        col.add(marshaller.unmarshal(reader));
                    }
                    
                } else {
                    
                    PropertyEditor editor = PropertyEditorManager.findEditor(propType);
                    if (editor != null) {
                        editor.setAsText(reader.getElementText());
                        pd.getWriteMethod().invoke(modelObject, editor.getValue());
                    }
                    
                }
                
            }
            
            return modelObject;
            
        } catch (InstantiationException ex) {
            throw new MarshalException(ex);
        } catch (IllegalAccessException ex) {
            throw new MarshalException(ex);
        } catch (XMLStreamException ex) {
            throw new MarshalException(ex);
        } catch (IllegalArgumentException ex) {
            throw new MarshalException(ex);
        } catch (InvocationTargetException ex) {
            throw new MarshalException(ex);
        }
        
    }
    
    /**
     * Marshalls the physical component definition to the specified stream writer.
     * 
     * @param modelObject Physical component definition to be serialized.
     * @param writer Stream writer to which the infoset is serialized.
     * @throws MarshalException In case of any marshalling error.
     */
    public void marshal(Object modelObject, XMLStreamWriter writer) throws MarshalException {
        
        try {
            
            writer.writeStartElement(xmlName.getLocalPart());
            writer.writeNamespace(null, xmlName.getNamespaceURI());
            
            for(PropertyDescriptor pd : propertyDescriptors.values()) {
                
                String name = pd.getName();
                if("class".equals(name)) {
                    continue;
                }
                
                writer.writeStartElement(name);
                Object prop = pd.getReadMethod().invoke(modelObject);
                
                if(prop instanceof ModelObject) {
                    Marshaller marshaller = registry.getMarshaller(prop.getClass());
                    marshaller.marshal(prop, writer);
                } else if(prop instanceof Collection) {
                    for(Object obj : (Collection) prop) {
                        Marshaller marshaller = registry.getMarshaller(obj.getClass());
                        marshaller.marshal(obj, writer);
                    }
                } else {
                    writer.writeCharacters(String.valueOf(prop));
                }
                
                writer.writeEndElement();
                
            }
            
            writer.writeEndElement();
            
        } catch (XMLStreamException ex) {
            throw new MarshalException(ex);
        } catch (IllegalAccessException ex) {
            throw new MarshalException(ex);
        } catch (InvocationTargetException ex) {
            throw new MarshalException(ex.getTargetException());
        }
        
    }

}
