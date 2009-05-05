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
package org.fabric3.jaxb.serializer;

import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.fabric3.spi.binding.serializer.SerializationException;
import org.fabric3.spi.binding.serializer.Serializer;
import org.fabric3.spi.binding.serializer.UnsupportedTypesException;

/**
 * Serializes JAXB objects to XML. Currently, only string-based serialization is supported but enhancements to support alternative formats such as
 * binary streams could be supported.
 *
 * @version $Revision$ $Date$
 */
public class JAXBSerializer implements Serializer {
    private JAXBContext jaxbContext;

    public JAXBSerializer(JAXBContext jaxbContext) {
        this.jaxbContext = jaxbContext;
    }

    public <T> T serialize(Class<T> clazz, Object message) throws SerializationException {
        if (!String.class.equals(clazz)) {
            throw new UnsupportedTypesException("This implementation only supports serialization to Strings");
        }
        StringWriter writer = new StringWriter();
        try {
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.marshal(message, writer);
            return clazz.cast(writer.toString());
        } catch (JAXBException e) {
            throw new SerializationException(e);
        }
    }

    public <T> T serializeFault(Class<T> clazz, Throwable exception) throws SerializationException {
        return serialize(clazz, exception);
    }

    public <T> T deserialize(Class<T> clazz, Object serialized) throws SerializationException {
        if (!String.class.equals(serialized.getClass())) {
            throw new UnsupportedTypesException("This implementation only supports serialization from Strings");
        }
        try {
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            return clazz.cast(unmarshaller.unmarshal(new StringReader((String) serialized)));
        } catch (JAXBException e) {
            throw new SerializationException(e);
        }
    }

    public Throwable deserializeFault(Object serialized) throws SerializationException {
        return deserialize(Throwable.class, serialized);
    }
}
