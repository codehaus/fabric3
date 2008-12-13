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
package org.fabric3.management.domain;

/**
 * Denotes a general exception performing a deployment operation. This exception may be propagated to management clients.
 *
 * @version $Revision$ $Date$
 */
public class DeploymentManagementException extends Exception {
    private static final long serialVersionUID = 5748693619945698434L;

    public DeploymentManagementException(String message) {
        super(message);
    }

    public DeploymentManagementException(String message, Throwable cause) {
        super(message, cause);
    }

    public DeploymentManagementException(Throwable cause) {
        super(cause);
    }
}