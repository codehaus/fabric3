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
package org.fabric3.maven;

import org.fabric3.api.Fabric3RuntimeException;

/**
 * Exception thrown in case of an artifact error.
 *
 * @version $Rev$ $Date$
 */
public class Fabric3DependencyException extends Fabric3RuntimeException {

    /**
     * Initializes the cause.
     *
     * @param cause Cause of the exception.
     */
    public Fabric3DependencyException(Throwable cause) {
        super(cause);
    }

    /**
     * Initializes the message.
     *
     * @param message Message of the exception.
     */
    public Fabric3DependencyException(String message) {
        super(message);
    }


    /**
     * Initializes the message.
     *
     * @param message    Message of the exception.
     * @param identifier an identifier for the exeption.
     */
    public Fabric3DependencyException(String message, String identifier) {
        super(message, identifier);
    }
}
