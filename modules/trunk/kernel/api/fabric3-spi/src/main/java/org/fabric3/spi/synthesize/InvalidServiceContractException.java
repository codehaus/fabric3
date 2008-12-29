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
 * --- Original Apache License ---
 *
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
package org.fabric3.spi.synthesize;

import java.util.List;

import org.fabric3.host.contribution.ValidationFailure;
import org.fabric3.spi.introspection.xml.XmlValidationFailure;

/**
 * @version $Rev: 4336 $ $Date: 2008-05-25 02:06:15 -0700 (Sun, 25 May 2008) $
 */
public class InvalidServiceContractException extends ComponentRegistrationException {
    private static final long serialVersionUID = 4367622270403828483L;
    private List<ValidationFailure> errors;

    public InvalidServiceContractException(List<ValidationFailure> errors) {
        super("System service contract has errors");
        this.errors = errors;
    }

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
                b.append("ERROR: ").append(failure.getMessage()).append("\n");
            } else {
                b.append("ERROR: ").append(failure);
            }
            b.append("\n");
        }
        return b.toString();
    }


}
