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
 * Provides transport-independent access to header information transmitted as part of a remote service invocation.
 *
 * @version $Revision$ $Date$
 */
public interface HeaderContext {

    /**
     * Returns the content length of the invocation message.
     *
     * @return the content length of the invocation message
     */
    long getContentLength();

    /**
     * Returns the name of the service operation being invoked.
     *
     * @return the the name of the service operation being invoked
     */
    String getOperationName();

    /**
     * Returns the routing information serialized as a string.
     *
     * @return the routing information serialized
     */
    String getRoutingText();

    /**
     * Returns the routing information serialized as a byte array.
     *
     * @return the routing information serialized
     */
    byte[] getRoutingBytes();

}
