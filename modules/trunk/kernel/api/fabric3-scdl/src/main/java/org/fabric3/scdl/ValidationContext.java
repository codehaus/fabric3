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
package org.fabric3.scdl;

import java.util.ArrayList;
import java.util.List;

/**
 * Context for gathering the results of model validation.
 *
 * @version $Rev$ $Date$
 */
public class ValidationContext {
    private final List<ValidationFailure> errors = new ArrayList<ValidationFailure>();
    private final List<ValidationFailure> warnings = new ArrayList<ValidationFailure>();

    /**
     * Returns true if the validation has detected any fatal errors.
     *
     * @return true if the validation has detected any fatal errors
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    /**
     * Returns the list of fatal errors detected during validation.
     *
     * @return the list of fatal errors detected during validation
     */
    public List<ValidationFailure> getErrors() {
        return errors;
    }

    /**
     * Add a fatal error to the validation results.
     *
     * @param e the fatal error that has been found
     */
    public void addError(ValidationFailure e) {
        errors.add(e);
    }

    /**
     * Returns true if the validation has detected any non-fatal warnings.
     *
     * @return true if the validation has detected any non-fatal warnings
     */
    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }

    /**
     * Returns the list of non-fatal warnings detected during validation.
     *
     * @return the list of non-fatal warnings detected during validation
     */
    public List<ValidationFailure> getWarnings() {
        return warnings;
    }

    /**
     * Add a non-fatal warning to the validation results.
     *
     * @param e the non-fatal warning that has been found
     */
    public void addWarning(ValidationFailure e) {
        warnings.add(e);
    }
}
