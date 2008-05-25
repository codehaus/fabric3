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

import java.util.List;

import org.fabric3.host.contribution.ValidationFailure;

/**
 * Context for gathering the results of model validation.
 * <p/>
 * The context allows both errors and warnings to be gathered. Errors indicate problems that will definitely prevent the model object from being
 * activated such as a missing implementation. Warnings indicate issues that are not in themselves fatal but which may result in an activation
 * failure. For example, a composite service may promote a component that it does not contain. This is likely to be an error but would successfully
 * activate if the component was defined by another composite that included this one.
 *
 * @version $Rev$ $Date$
 */
public interface ValidationContext {

    /**
     * Returns true if the validation has detected any fatal errors.
     *
     * @return true if the validation has detected any fatal errors
     */
    boolean hasErrors();

    /**
     * Returns the list of fatal errors detected during validation.
     *
     * @return the list of fatal errors detected during validation
     */
    List<ValidationFailure> getErrors();

    /**
     * Add a fatal error to the validation results.
     *
     * @param e the fatal error that has been found
     */
    void addError(ValidationFailure e);

    /**
     * Add a collection of fatal errors to the validation results.
     *
     * @param errors the fatal errors that have been found
     */
    void addErrors(List<ValidationFailure> errors);

    /**
     * Returns true if the validation has detected any non-fatal warnings.
     *
     * @return true if the validation has detected any non-fatal warnings
     */
    boolean hasWarnings();

    /**
     * Returns the list of non-fatal warnings detected during validation.
     *
     * @return the list of non-fatal warnings detected during validation
     */
    List<ValidationFailure> getWarnings();

    /**
     * Add a non-fatal warning to the validation results.
     *
     * @param e the non-fatal warning that has been found
     */
    void addWarning(ValidationFailure e);


    /**
     * Add a collection of non-fatal warnings to the validation results.
     *
     * @param warnings the non-fatal warnings that have been found
     */
    void addWarnings(List<ValidationFailure> warnings);

}
