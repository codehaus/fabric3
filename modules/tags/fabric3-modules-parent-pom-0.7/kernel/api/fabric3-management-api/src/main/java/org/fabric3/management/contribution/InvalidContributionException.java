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
package org.fabric3.management.contribution;

import java.util.List;

/**
 * Denotes an invalid contribution and reports introspection errors.
 *
 * @version $Revision$ $Date$
 */
public class InvalidContributionException extends ContributionInstallException {
    private static final long serialVersionUID = -9209475021865946685L;
    private List<ErrorInfo> errors;

    public InvalidContributionException(String message, List<ErrorInfo> errors) {
        super(message);
        this.errors = errors;
    }

    public List<ErrorInfo> getErrors() {
        return errors;
    }
}
