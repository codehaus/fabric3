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
package org.fabric3.host.contribution;

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
