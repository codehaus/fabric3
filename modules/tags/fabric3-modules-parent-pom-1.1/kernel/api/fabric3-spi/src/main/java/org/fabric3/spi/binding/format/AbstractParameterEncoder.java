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

/**
 * Convenience class for ParameterEncoder implementations.
 *
 * @version $Revision$ $Date$
 */
public abstract class AbstractParameterEncoder implements ParameterEncoder {

    public String encodeText(Message message) throws EncoderException {
        throw new UnsupportedOperationException();
    }

    public byte[] encodeBytes(Message message) throws EncoderException {
        throw new UnsupportedOperationException();
    }

    public Object decode(String operationName, String body) throws EncoderException {
        throw new UnsupportedOperationException();
    }

    public Object decode(String operationName, byte[] body) throws EncoderException {
        throw new UnsupportedOperationException();
    }

    public Object decodeResponse(String operationName, String serialized) throws EncoderException {
        throw new UnsupportedOperationException();
    }

    public Object decodeResponse(String operationName, byte[] serialized) throws EncoderException {
        throw new UnsupportedOperationException();
    }

    public Throwable decodeFault(String operationName, String body) throws EncoderException {
        throw new UnsupportedOperationException();
    }

    public Throwable decodeFault(String operationName, byte[] body) throws EncoderException {
        throw new UnsupportedOperationException();
    }

}
