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

/**
 * Callback for {@link MessageEncoder} clients to receive notification of encoded message header information.
 *
 * @version $Revision$ $Date$
 */
public interface EncodeCallback {

    /**
     * Callback for the encoded message content length.
     *
     * @param length the encoded content length
     */
    void encodeContentLengthHeader(long length);

    /**
     * Callback for the encoded operation name.
     *
     * @param name the encoded operation name
     */
    void encodeOperationHeader(String name);

    /**
     * Callback for the routing information encoded as a string.
     *
     * @param header the encoded routing information
     */
    void encodeRoutingHeader(String header);

    /**
     * Callback for the routing information encoded as a byte array.
     *
     * @param header the encoded routing information
     */
    void encodeRoutingHeader(byte[] header);

}
