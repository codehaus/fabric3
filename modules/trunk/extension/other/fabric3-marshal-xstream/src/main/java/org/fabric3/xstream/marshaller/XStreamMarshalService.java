/*
 * See the NOTICE file distributed with this work for information
 * regarding copyright ownership.  This file is licensed
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
package org.fabric3.xstream.marshaller;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.spi.marshaller.MarshalException;
import org.fabric3.spi.marshaller.MarshalService;
import org.fabric3.spi.marshaller.DelegatingMarshalService;
import org.fabric3.xstream.factory.ClassLoaderStaxDriver;
import org.fabric3.xstream.factory.XStreamFactory;

/**
 * XStream-based implementation of the MarshalService.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class XStreamMarshalService implements MarshalService {
    private XStream xStream;
    private StaxDriver staxDriver;
    private DelegatingMarshalService marshalService;

    public XStreamMarshalService(@Reference XStreamFactory factory, @Reference DelegatingMarshalService service) {
        xStream = factory.createInstance();
        staxDriver = new ClassLoaderStaxDriver(getClass().getClassLoader());
        this.marshalService = service;
    }

    public void marshall(Object modelObject, XMLStreamWriter writer) throws MarshalException {
        try {
            xStream.marshal(modelObject, staxDriver.createStaxWriter(writer));
        } catch (XMLStreamException ex) {
            throw new MarshalException(ex);
        }
    }

    public <T> T unmarshall(Class<T> type, XMLStreamReader reader) throws MarshalException {
        return type.cast(xStream.unmarshal(staxDriver.createStaxReader(reader)));
    }

    @Init
    public void init() {
        marshalService.registerMarshalService(this);
    }
}
