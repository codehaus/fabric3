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

/**
 * Base class for validation failures.
 *
 * @version $Rev$ $Date$
 */
public abstract class ValidationFailure<T> {
    private final T validatable;

    /**
     * Constructor specifying the validatable object associated with this failure.
     *
     * @param modelObject the model object associated with this failure
     */
    protected ValidationFailure(T modelObject) {
        this.validatable = modelObject;
    }

    /**
     * Returns the object that failed validation.
     *
     * @return the object that failed validation
     */
    public T getValidatable() {
        return validatable;
    }

    public String getMessage() {
        return getClass().getName();
    }
}
