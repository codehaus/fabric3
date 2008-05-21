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
package org.fabric3.introspection.contract;

import java.util.List;

import org.fabric3.introspection.IntrospectionException;
import org.fabric3.introspection.xml.XmlValidationFailure;
import org.fabric3.scdl.ValidationFailure;

/**
 * @version $Rev$ $Date$
 */
public class InvalidServiceContractException extends IntrospectionException {
    private static final long serialVersionUID = 4367622270403828483L;
    private List<ValidationFailure> errors;

    protected InvalidServiceContractException() {
    }

    protected InvalidServiceContractException(String message) {
        super(message);
    }

    protected InvalidServiceContractException(String message, String identifier) {
        super(message, identifier);
    }

    protected InvalidServiceContractException(String message, Throwable cause) {
        super(message, cause);
    }

    protected InvalidServiceContractException(String message, String identifier, Throwable cause) {
        super(message, identifier, cause);
    }

    protected InvalidServiceContractException(Throwable cause) {
        super(cause);
    }

    public InvalidServiceContractException(List<ValidationFailure> errors) {
        this.errors = errors;
    }

    // xcv temp remove
    public String getMessage() {
        if (errors == null) {
            return super.getMessage();
        }
        StringBuilder b = new StringBuilder();
        if (errors.size() == 1) {
            b.append("1 error was detected: \n");
        } else {
            b.append(errors.size()).append(" errors were detected: \n");
        }
        for (ValidationFailure failure : errors) {
            if (failure instanceof XmlValidationFailure) {
                b.append("ERROR: ").append(((XmlValidationFailure) failure).getMessage()).append("\n");
            } else {
                b.append("ERROR: ").append(failure);
            }
            b.append("\n");
        }
        return b.toString();
    }


}
