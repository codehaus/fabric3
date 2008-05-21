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
package org.fabric3.scdl.validation;

import org.fabric3.scdl.ValidationFailure;

/**
 * Denotes a missing resource such as a class file.
 *
 * @version $Revision$ $Date$
 */
public class MissingResource extends ValidationFailure<String> {
    private String description;

    public MissingResource(String description, String name) {
        super(name);
        this.description = description;
    }

    public String getMessage() {
        return description + ": " + getModelObject();
    }
}
