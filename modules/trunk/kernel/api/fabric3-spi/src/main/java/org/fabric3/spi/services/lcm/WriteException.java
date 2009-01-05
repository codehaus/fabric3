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
package org.fabric3.spi.services.lcm;

import org.fabric3.host.Fabric3Exception;


/**
 * Denotes an exception recording an logical component operation
 *
 * @version $Rev$ $Date$
 */
public class WriteException extends Fabric3Exception {
    private static final long serialVersionUID = 652954682135057498L;

    public WriteException(String message, Throwable cause) {
        super(message, cause);
    }

    public WriteException(String message, String identifier, Throwable cause) {
        super(message, identifier, cause);
    }
}
