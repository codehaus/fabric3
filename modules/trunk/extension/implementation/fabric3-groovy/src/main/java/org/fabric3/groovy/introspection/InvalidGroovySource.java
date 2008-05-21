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
package org.fabric3.groovy.introspection;

import org.fabric3.scdl.ValidationFailure;

/**
 * @version $Revision$ $Date$
 */
public class InvalidGroovySource extends ValidationFailure<String> {
    private Throwable cause;

    protected InvalidGroovySource(String source, Throwable cause) {
        super(source);
        this.cause = cause;
    }

    public Throwable getCause() {
        return cause;
    }

    public String getMessage() {
        return "Groovy source file was invalid: \n"+ cause.toString();
    }
}
