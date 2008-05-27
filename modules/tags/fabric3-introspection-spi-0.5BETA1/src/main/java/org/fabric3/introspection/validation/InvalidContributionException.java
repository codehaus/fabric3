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
import java.util.ArrayList;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

import org.fabric3.host.contribution.ValidationException;
import org.fabric3.host.contribution.ValidationFailure;

/**
 * @version $Revision$ $Date$
 */
public class InvalidContributionException extends ValidationException {
    private static final long serialVersionUID = -5729273092766880963L;

    /**
     * Constructor.
     *
     * @param errors   the errors that were found during validation
     * @param warnings the warnings that were found during validation
     */
    public InvalidContributionException(List<ValidationFailure> errors, List<ValidationFailure> warnings) {
        super(errors, warnings);
    }

    /**
     * Constructor.
     *
     * @param errors the errors that were found during validation
     */
    public InvalidContributionException(List<ValidationFailure> errors) {
        super(errors, new ArrayList<ValidationFailure>());
    }

    public String getMessage() {
        ByteArrayOutputStream bas = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(bas);
        if (!getErrors().isEmpty()) {
            ValidationUtils.writeErrors(writer, getErrors());
            writer.write("\n");
        }
        if (!getWarnings().isEmpty()) {
            ValidationUtils.writeWarnings(writer, getWarnings());
        }
        return bas.toString();
    }

}
