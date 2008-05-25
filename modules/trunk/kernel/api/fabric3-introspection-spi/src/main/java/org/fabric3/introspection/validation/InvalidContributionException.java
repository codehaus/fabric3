/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.fabric3.introspection.validation;

import java.util.List;
import java.util.Collections;
import java.util.ArrayList;

import org.fabric3.scdl.ModelObject;
import org.fabric3.scdl.ValidationFailure;
import org.fabric3.scdl.ArtifactValidationFailure;

/**
 * @version $Revision$ $Date$
 */
public class InvalidContributionException extends ValidationException {
    private static final long serialVersionUID = -5729273092766880963L;
    private static ValidationExceptionComparator COMPARATOR = new ValidationExceptionComparator();

    /**
     * Constructor indicating which composite is invalid and what the failures were.
     *
     * @param failures the errors that were found during validation
     */
    public InvalidContributionException(List<ValidationFailure> failures) {
        super(failures);
    }

    @Override
    public ModelObject getModelObject() {
        return null;
    }

    public String getMessage() {
        StringBuilder b = new StringBuilder();
        int count = 0;
        List<ValidationFailure> sorted = new ArrayList<ValidationFailure>(getFailures());
        // sort the errors so that ArtifactValidationFailures are evaluated last. This is done so that nested failures are printed after all
        // failures in the parent artifact.
        Collections.sort(sorted, COMPARATOR);
        for (ValidationFailure failure : sorted) {
            count = reportContributionError(failure, b, count);
        }
        if (count == 1) {
            b.append("1 error was found \n\n");
        } else {
            b.append(count).append(" errors were found \n\n");
        }
        return b.toString();
    }

    protected int reportContributionError(ValidationFailure failure, StringBuilder b, int count) {
        if (failure instanceof ArtifactValidationFailure) {
            ArtifactValidationFailure artifactFailure = (ArtifactValidationFailure) failure;
            if (!errorsOnlyInContainedArtifacts(artifactFailure)) {
                b.append("Errors in ").append(artifactFailure.getArtifactName()).append("\n\n");
            }
            for (ValidationFailure childFailure : artifactFailure.getFailures()) {
                count = reportContributionError(childFailure, b, count);
            }
        } else {
            b.append("  ERROR: ").append(failure.getMessage()).append("\n\n");
            ++count;
        }
        return count;
    }

    private boolean errorsOnlyInContainedArtifacts(ArtifactValidationFailure artifactFailure) {
        for (ValidationFailure failure : artifactFailure.getFailures()) {
            if (!(failure instanceof ArtifactValidationFailure)) {
                return false;
            }
        }
        return true;
    }

}
