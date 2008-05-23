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

import org.fabric3.scdl.ModelObject;
import org.fabric3.scdl.ValidationFailure;

/**
 * @version $Revision$ $Date$
 */
public class InvalidContributionException extends ValidationException{
    private static final long serialVersionUID = -5729273092766880963L;

    /**
     * Constructor indicating which composite is invalid and what the failures were.
     *
     * @param failures  the errors that were found during validation
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
        if (getFailures().size() == 1) {
            b.append("1 error was detected: \n");
        } else {
            b.append(getFailures().size()).append(" errors were detected: \n");
        }
        for (ValidationFailure failure : getFailures()) {
            b.append("ERROR: ").append(failure.getMessage()).append("\n\n");
        }
        return b.toString();
    }
}
