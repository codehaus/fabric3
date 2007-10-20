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
package org.fabric3.fabric.marshaller.xstream;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.fabric3.spi.marshaller.MarshalException;
import org.fabric3.spi.marshaller.Marshaller;
import org.fabric3.spi.marshaller.MarshallerRegistry;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

/**
 * @version $Revision$ $Date$
 */
public class XStreamMarshallerRegistry implements MarshallerRegistry {
    
    /**
     * Thread safe object for marshalling.
     */
    private XStream xStream = new XStream();
    
    /**
     * Thread safe Stax driver.
     */
    private StaxDriver staxDriver = new StaxDriver();
    
    /**
     * Register the converters.
     */
    public XStreamMarshallerRegistry() {
        // TODO Register the converters
    }

    /**
     * Not supported, handled internally.
     */
    public Marshaller getMarshaller(Class<?> modelClass) throws MarshalException {
        throw new UnsupportedOperationException();
    }

    /**
     * Not supported, handled internally.
     * 
     */
    public Marshaller getMarshaller(QName xmlName) throws MarshalException {
        throw new UnsupportedOperationException();
    }

    /**
     * Not supported, handled internally.
     * 
     */
    public void registerMarshaller(Class<?> arg0, QName arg1, Marshaller arg2) {
        throw new UnsupportedOperationException();
    }

    /**
     * Marshals using XStream.
     */
    public void marshall(Object modelObject, XMLStreamWriter writer) throws MarshalException {
        
        try {
            xStream.marshal(modelObject, staxDriver.createStaxWriter(writer));
        } catch (XMLStreamException ex) {
            throw new MarshalException(ex);
        }
    }

    /**
     * Unmarshals using xstream.
     */
    public Object unmarshall(XMLStreamReader reader) throws MarshalException {
        return xStream.unmarshal(staxDriver.createStaxReader(reader));
    }

}
