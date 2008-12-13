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
package org.fabric3.xstream.marshaller;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.spi.services.marshaller.MarshalException;
import org.fabric3.spi.services.marshaller.MarshalService;
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

    public XStreamMarshalService(@Reference XStreamFactory factory) {
        xStream = factory.createInstance();
        staxDriver = new ClassLoaderStaxDriver(getClass().getClassLoader());
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

}
