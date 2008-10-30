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

import java.util.List;

import org.fabric3.management.contribution.ContributionManagementException;

/**
 * Used to report deployment errors.
 *
 * @version $Revision$ $Date$
 */
public class InvalidDeploymentException extends DeploymentManagementException {
    private static final long serialVersionUID = -4240726635386110545L;
    private List<String> errors;

    public InvalidDeploymentException(String message, List<String> errors) {
        super(message);
        this.errors = errors;
    }

    public List<String> getErrors() {
        return errors;
    }
}