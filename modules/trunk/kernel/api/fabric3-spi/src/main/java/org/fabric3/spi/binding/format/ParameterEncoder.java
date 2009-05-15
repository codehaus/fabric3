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
 * Encodes in-, out- and fault parameters.
 *
 * @version $Revision$ $Date$
 */
public interface ParameterEncoder {

    /**
     * Encodes the parameters for a service invocation as a string.
     *
     * @param message the invocation message
     * @return the encoded parameters
     * @throws EncoderException if an encoding error occurs
     */
    String encodeText(Message message) throws EncoderException;

    /**
     * Encodes the parameters for a service invocation as a byte array.
     *
     * @param message the invocation message
     * @return the encoded parameters
     * @throws EncoderException if an encoding error occurs
     */
    byte[] encodeBytes(Message message) throws EncoderException;

    /**
     * Decodes the parameters for a service invocation  encoded as a string on the service provider side of a wire.
     *
     * @param operationName the name of the operation being invoked
     * @param encoded       the encoded parameters
     * @return the decoded parameters
     * @throws EncoderException if a decoding error occurs
     */
    Object decode(String operationName, String encoded) throws EncoderException;

    /**
     * Decodes the parameters for a service invocation encoded as a byte array on the service provider side of a wire.
     *
     * @param operationName the name of the operation being invoked
     * @param encoded       the encoded parameters
     * @return the decoded parameters
     * @throws EncoderException if a decoding error occurs
     */
    Object decode(String operationName, byte[] encoded) throws EncoderException;

    /**
     * Decodes a service invocation response as a string on the client side of a wire.
     *
     * @param operationName the name of the operation being invoked
     * @param encoded       the encoded response
     * @return the decoded response
     * @throws EncoderException if a decoding error occurs
     */
    Object decodeResponse(String operationName, String encoded) throws EncoderException;

    /**
     * Decodes a service invocation response as a byte array on the client side of a wire.
     *
     * @param operationName the name of the operation being invoked
     * @param encoded       the encoded response
     * @return the decoded response
     * @throws EncoderException if a decoding error occurs
     */
    Object decodeResponse(String operationName, byte[] encoded) throws EncoderException;

    /**
     * Decodes a service invocation fault response as a string on the client side of a wire.
     *
     * @param operationName the name of the operation being invoked
     * @param encoded       the encoded fault
     * @return the decoded fault
     * @throws EncoderException if a decoding error occurs
     */
    Throwable decodeFault(String operationName, String encoded) throws EncoderException;

    /**
     * Decodes a service invocation fault response as a byte array on the client side of a wire.
     *
     * @param operationName the name of the operation being invoked
     * @param encoded       the encoded fault
     * @return the decoded fault
     * @throws EncoderException if a decoding error occurs
     */
    Throwable decodeFault(String operationName, byte[] encoded) throws EncoderException;

}