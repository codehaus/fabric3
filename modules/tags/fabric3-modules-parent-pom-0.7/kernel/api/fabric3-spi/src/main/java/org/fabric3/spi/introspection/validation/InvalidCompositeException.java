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
package org.fabric3.spi.introspection.validation;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.List;
import javax.xml.namespace.QName;

import org.fabric3.host.contribution.ValidationException;
import org.fabric3.model.type.ValidationFailure;

/**
 * @version $Rev$ $Date$
 */
public class InvalidCompositeException extends ValidationException {
    private static final long serialVersionUID = -2678786389599538999L;

    private final QName name;

    /**
     * Constructor.
     *
     * @param name     the qualified name of the composite that failed validation
     * @param errors   the errors that were found during validation
     * @param warnings the warnings that were found during validation
     */
    public InvalidCompositeException(QName name, List<ValidationFailure> errors, List<ValidationFailure> warnings) {
        super(errors, warnings);
        this.name = name;
    }

    public QName getCompositeName() {
        return name;
    }

    public String getMessage() {
        ByteArrayOutputStream bas = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(bas);
        ValidationUtils.writeErrors(writer, getErrors());
        writer.write("\n");
        ValidationUtils.writeWarnings(writer, getWarnings());
        return bas.toString();
    }

}
