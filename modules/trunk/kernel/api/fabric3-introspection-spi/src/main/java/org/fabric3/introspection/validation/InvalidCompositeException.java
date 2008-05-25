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
import javax.xml.namespace.QName;

import org.fabric3.host.contribution.ValidationFailure;
import org.fabric3.host.contribution.ValidationException;

/**
 * @version $Rev$ $Date$
 */
public class InvalidCompositeException extends ValidationException {
    private static final long serialVersionUID = -2678786389599538999L;

    private final QName name;

    /**
     * Constructor indicating which composite is invalid and what the failures were.
     *
     * @param name the qualified name of the composite that failed validation
     * @param failures  the errors that were found during validation
     */
    public InvalidCompositeException(QName name, List<ValidationFailure> failures) {
        super(failures);
        this.name = name;
    }

    public QName getCompositeName() {
        return name;
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
