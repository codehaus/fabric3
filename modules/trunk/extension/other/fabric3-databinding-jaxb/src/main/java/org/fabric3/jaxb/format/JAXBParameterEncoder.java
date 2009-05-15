/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the ÒLicenseÓ), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an Òas isÓ basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.jaxb.format;

import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.fabric3.spi.binding.format.AbstractParameterEncoder;
import org.fabric3.spi.binding.format.EncoderException;
import org.fabric3.spi.invocation.Message;

/**
 * ParameterEncoder that uses JAXB. Currently, only string-based serialization is supported but enhancements to support alternative formats such as
 * binary streams could be supported.
 *
 * @version $Revision$ $Date$
 */
public class JAXBParameterEncoder extends AbstractParameterEncoder {
    private JAXBContext jaxbContext;

    /**
     * Constructor.
     *
     * @param jaxbContext the JAXBContext to use for de/serialization.
     */
    public JAXBParameterEncoder(JAXBContext jaxbContext) {
        this.jaxbContext = jaxbContext;
    }

    @Override
    public String encodeText(Message message) throws EncoderException {
        return serialize(message);
    }

    @Override
    public Object decode(String operationName, String body) throws EncoderException {
        return deserialize(body);
    }

    @Override
    public Object decodeResponse(String operationName, String serialized) throws EncoderException {
        return decode(operationName, serialized);
    }

    @Override
    public Throwable decodeFault(String operationName, String serialized) throws EncoderException {
        try {
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            StringReader reader = new StringReader(serialized);
            return (Throwable) unmarshaller.unmarshal(reader);
        } catch (JAXBException e) {
            throw new EncoderException(e);
        }
    }

    private String serialize(Message message) throws EncoderException {
        Object body = message.getBody();
        if (body != null && body.getClass().isArray() && !body.getClass().isPrimitive()) {
            Object[] payload = (Object[]) body;
            if (payload.length > 1) {
                throw new UnsupportedOperationException("Multiple paramters not supported");
            }
            body = payload[0];
        }
        StringWriter writer = new StringWriter();
        try {
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.marshal(body, writer);
            return writer.toString();
        } catch (JAXBException e) {
            throw new EncoderException(e);
        }
    }

    private Object deserialize(String body) throws EncoderException {
        StringReader reader = new StringReader(body);
        try {
            Unmarshaller marshaller = jaxbContext.createUnmarshaller();
            return marshaller.unmarshal(reader);
        } catch (JAXBException e) {
            throw new EncoderException(e);
        }
    }


}