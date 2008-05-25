package org.fabric3.introspection.validation;

import java.util.Comparator;

import org.fabric3.scdl.ArtifactValidationFailure;
import org.fabric3.scdl.ValidationFailure;

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
