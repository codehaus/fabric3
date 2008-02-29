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
package org.fabric3.fabric.services.contribution.manifest;

import org.fabric3.spi.services.contribution.ManifestLoadException;

/**
 * Thrown when a required element attribute is not specified in a contribution manifest
 *
 * @version $Rev$ $Date$
 */
public class MissingAttributeException extends ManifestLoadException {
    private static final long serialVersionUID = 2966463108475506318L;

    public MissingAttributeException(String message) {
        super(message, null);
    }

    public MissingAttributeException(String message, String identifier) {
        super(message, identifier);
    }

}
