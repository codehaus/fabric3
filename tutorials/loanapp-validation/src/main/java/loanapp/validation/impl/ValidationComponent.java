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
package loanapp.validation.impl;

import loanapp.validation.RequestValidator;
import loanapp.validation.ValidationService;
import loanapp.validation.ValidationResult;
import loanapp.validation.DataError;
import loanapp.message.LoanRequest;

import java.util.List;
import java.util.ArrayList;

import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Scope;

/**
 * Implementation of ValidationService that delegates to a series of RequestValidators.
 *
 * @version $Revision$ $Date$
 */
@Scope("COMPOSITE")
public class ValidationComponent implements ValidationService {
    private List<RequestValidator> validators = new ArrayList<RequestValidator>();

    @Reference(required = false)
    public void setValidators(List<RequestValidator> validators) {
        this.validators = validators;
    }

    public ValidationResult validateRequest(LoanRequest request) {
        List<DataError> cumulative = new ArrayList<DataError>();
        for (RequestValidator validator : validators) {
            List<DataError> errors = validator.validate(request);
            cumulative.addAll(errors);
        }
        return new ValidationResult(cumulative);
    }
}
