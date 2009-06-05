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
package org.fabric3.binding.jms.runtime.lookup;

import org.fabric3.host.Fabric3Exception;

/**
 * Denotes an error looking up a connection factory or destination.
 *
 * @version $Revision$ $Date$
 */
public class JmsLookupException extends Fabric3Exception {
    private static final long serialVersionUID = -2022498980450562546L;

    public JmsLookupException(String message) {
        super(message);
    }

    public JmsLookupException(String message, Throwable cause) {
        super(message, cause);
    }
}
