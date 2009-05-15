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
package org.fabric3.spi.binding.format;

import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.util.Base64;

/**
 * Base class for WireFormatters that use custom serialization formats.
 *
 * @version $Revision$ $Date$
 */
public abstract class BaseParameterEncoder extends AbstractParameterEncoder {
    private ClassLoader loader;

    /**
     * Cosntructor
     *
     * @param loader the classloader defining message body content types
     */
    protected BaseParameterEncoder(ClassLoader loader) {
        this.loader = loader;
    }

    public byte[] encodeBytes(Message message) throws EncoderException {
        // serialize the body
        Object body = message.getBody();
        if (body != null && body.getClass().isArray() && !body.getClass().getComponentType().isPrimitive()) {
            Object[] array = (Object[]) body;
            if (array.length == 1) {
                body = array[0];
            } else if (array.length > 1) {
                throw new UnsupportedOperationException("Multiple paramters not supported");
            }
        }
        return serialize(body);
    }

    @Override
    public String encodeText(Message message) throws EncoderException {
        return Base64.encode(encodeBytes(message));
    }

    @Override
    public Object decode(String operationName, String body) throws EncoderException {
        return deserialize(Object.class, Base64.decode(body), loader);
    }

    @Override
    public Object decode(String operationName, byte[] serialized) throws EncoderException {
        return deserialize(Object.class, serialized, loader);
    }

    @Override
    public Object decodeResponse(String operationName, String serialized) throws EncoderException {
        return deserialize(Object.class, Base64.decode(serialized), loader);
    }

    @Override
    public Object decodeResponse(String operationName, byte[] serialized) throws EncoderException {
        return deserialize(Object.class, serialized, loader);
    }

    public Throwable decodeFault(String operationName, byte[] body) throws EncoderException {
        return deserialize(Throwable.class, body, loader);
    }

    public Throwable decodeFault(String operationName, String serialized) throws EncoderException {
        return decodeFault(operationName, Base64.decode(serialized));
    }

    protected abstract byte[] serialize(Object o) throws EncoderException;

    protected abstract <T> T deserialize(Class<T> clazz, byte[] bytes, ClassLoader cl) throws EncoderException;

}
