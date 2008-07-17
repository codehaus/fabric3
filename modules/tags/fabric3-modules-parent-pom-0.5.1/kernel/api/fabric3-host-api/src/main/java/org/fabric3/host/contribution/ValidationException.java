/*
 * See the NOTICE file distributed with this work for information
 * regarding copyright ownership.  This file is licensed
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
package org.fabric3.host.contribution;

import java.util.Collections;
import java.util.List;

/**
 * Base class for exceptions indicating a contribution has failed validation.
 *
 * @version $Rev$ $Date$
 */
public abstract class ValidationException extends ContributionException {
    private static final long serialVersionUID = -9097590343387033730L;

    private final List<ValidationFailure> errors;
    private final List<ValidationFailure> warnings;

    /**
     * Constructor that initializes the initial list of errors and warnings.
     *
     * @param errors   the list of errors
     * @param warnings the list of warnings
     */
    protected ValidationException(List<ValidationFailure> errors, List<ValidationFailure> warnings) {
        super("Validation errors were found");
        this.errors = errors;
        this.warnings = warnings;
    }

    /**
     * Returns a collection of underlying errors associated with this exception.
     *
     * @return the collection of underlying errors
     */
    public List<ValidationFailure> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    /**
     * Returns a collection of underlying warnings associated with this exception.
     *
     * @return the collection of underlying errors
     */
    public List<ValidationFailure> getWarnings() {
        return Collections.unmodifiableList(warnings);
    }

}
