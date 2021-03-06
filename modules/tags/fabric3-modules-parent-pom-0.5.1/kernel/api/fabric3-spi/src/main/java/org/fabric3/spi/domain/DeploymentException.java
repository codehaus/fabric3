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
package org.fabric3.spi.domain;

/**
 * Denotes an error during a deployment operation.
 *
 * @version $Rev$ $Date$
 */
public class DeploymentException extends DomainException {
    private static final long serialVersionUID = -8846536703004740119L;

    public DeploymentException(String message) {
        super(message);
    }

    public DeploymentException(String message, String identifier) {
        super(message, identifier);
    }

    public DeploymentException(String message, Throwable cause) {
        super(message, cause);
    }

    public DeploymentException(String message, String identifier, Throwable cause) {
        super(message, identifier, cause);
    }

    public DeploymentException(Throwable cause) {
        super(cause);
    }


}
