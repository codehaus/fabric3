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
package org.fabric3.introspection.validation;

import java.util.List;

import org.fabric3.scdl.Composite;
import org.fabric3.scdl.ValidationFailure;

/**
 * @version $Rev$ $Date$
 */
public class InvalidCompositeException extends ValidationException {
    private static final long serialVersionUID = -2678786389599538999L;

    private final Composite composite;

    /**
     * Constructor indicating which composite is invalid and what the failures were.
     *
     * @param composite the composite that failed validation
     * @param failures  the errors that were found during validation
     */
    public InvalidCompositeException(Composite composite, List<ValidationFailure> failures) {
        super(failures);
        this.composite = composite;
    }

    @Override
    public Composite getModelObject() {
        return composite;
    }
}
