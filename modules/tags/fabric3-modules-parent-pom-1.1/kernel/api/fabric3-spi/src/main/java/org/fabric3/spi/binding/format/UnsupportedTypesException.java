/*
 * Fabric3
 * Copyright � 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the �License�), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an �as is� basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.spi.binding.format;

/**
 * Raised when an attempt to serialize or deserialize an unsupported type is made.
 *
 * @version $Revision$ $Date$
 */
public class UnsupportedTypesException extends EncoderException {
    private static final long serialVersionUID = 4607391693877849373L;

    public UnsupportedTypesException(Throwable cause) {
        super(cause);
    }

    public UnsupportedTypesException(String message) {
        super(message);
    }
}