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
package org.fabric3.spi.introspection.validation;

import java.util.Comparator;

import org.fabric3.host.contribution.ValidationFailure;
import org.fabric3.host.contribution.ArtifactValidationFailure;

/**
 * Orders ValidationFailures. ArtifactValidationFailures are ordered after other types.
 *
 * @version $Revision$ $Date$
 */
public class ValidationExceptionComparator implements Comparator<ValidationFailure> {
    public int compare(ValidationFailure first, ValidationFailure second) {
        if (first instanceof ArtifactValidationFailure && !(second instanceof ArtifactValidationFailure)) {
            return -1;
        } else if (!(first instanceof ArtifactValidationFailure) && second instanceof ArtifactValidationFailure) {
            return 1;
        } else {
            return 0;
        }
    }
}
