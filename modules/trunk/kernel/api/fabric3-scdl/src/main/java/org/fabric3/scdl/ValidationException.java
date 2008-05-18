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

/**
 * Base class for exceptions indicating a model object has failed validation.
 *
 * @version $Rev$ $Date$
 */
public abstract class ValidationException extends Exception {
    private static final long serialVersionUID = -9097590343387033730L;

    private final List<ValidationFailure> failures;

    /**
     * Constructor that initializes the initial list of failures.
     *
     * @param failures the underlying failures of this exception
     */
    protected ValidationException(List<ValidationFailure> failures) {
        this.failures = failures;
    }

    /**
     * Returns the model object that raised this exception.
     *
     * @return the model object that raised this exception
     */
    public abstract ModelObject getModelObject();

    /**
     * Returns a collection of underlying failures of this exception.
     *
     * @return a collection of underlying failures
     */
    public List<ValidationFailure> getFailures() {
        return failures;
    }
}
