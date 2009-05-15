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
package org.fabric3.spi.binding.format;

import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.util.Base64;

/**
 * Base class for MessageEncoders that use custom serialization formats.
 *
 * @version $Revision$ $Date$
 */
public abstract class BaseMessageEncoder implements MessageEncoder {

    public String encodeText(String operationName, Message message, EncodeCallback callback) throws EncoderException {
        return Base64.encode(encode(message));
    }

    public byte[] encodeBytes(String operationName, Message message, EncodeCallback callback) throws EncoderException {
        return encode(message);
    }

    public String encodeResponseText(String operationName, Message message, ResponseEncodeCallback callback) throws EncoderException {
        String serialized = Base64.encode(encode(message));
        callback.encodeContentLengthHeader(serialized.length());
        return serialized;
    }

    public byte[] encodeResponseBytes(String operationName, Message message, ResponseEncodeCallback callback)
            throws EncoderException {
        byte[] serialized = encode(message);
        callback.encodeContentLengthHeader(serialized.length);
        return serialized;
    }

    public Message decodeResponse(byte[] serialized) throws EncoderException {
        return decode(Message.class, serialized);
    }

    public Message decodeResponse(String serialized) throws EncoderException {
        return decode(Message.class, Base64.decode(serialized));
    }

    public Message decodeFault(byte[] serialized) throws EncoderException {
        return decode(Message.class, serialized);
    }

    public Message decodeFault(String serialized) throws EncoderException {
        return decode(Message.class, Base64.decode(serialized));
    }


    public Message decode(byte[] serialized, HeaderContext context) throws EncoderException {
        return decode(Message.class, serialized);
    }

    public Message decode(String serialized, HeaderContext context) throws EncoderException {
        return decode(Message.class, Base64.decode(serialized));
    }

    /**
     * Encodes an object as a byte array.
     *
     * @param o the object to encode
     * @return the encoded object
     * @throws EncoderException if an encoding error occurs
     */
    protected abstract byte[] encode(Object o) throws EncoderException;

    /**
     * Decodes an object from a byte array.
     *
     * @param clazz the expected type
     * @param bytes byte array to decode
     * @return the decoded object
     * @throws EncoderException if an decoding error occurs
     */
    protected abstract <T> T decode(Class<T> clazz, byte[] bytes) throws EncoderException;


}