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
package org.fabric3.jxta.impl;

import org.fabric3.host.Fabric3RuntimeException;

/**
 * Runtime exception thrown with unexpected JXTA errors.
 *
 * @version $Revsion$ $Date$
 */
public class Fabric3JxtaException extends Fabric3RuntimeException {
    private static final long serialVersionUID = -7355964913702467901L;

    /**
     * Initializes the exception message and cause.
     *
     * @param message Exception message.
     * @param cause   Exception cause.
     */
    public Fabric3JxtaException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Initializes the exception message and identifier.
     *
     * @param message    Exception message.
     * @param identifier Exception identifier.
     */
    public Fabric3JxtaException(String message, String identifier) {
        super(message, identifier);
    }

    /**
     * Initializes the exception cause.
     *
     * @param cause Exception cause.
     */
    public Fabric3JxtaException(Throwable cause) {
        super(cause);
    }

    /**
     * Initializes the exception message.
     *
     * @param message Exception message.
     */
    public Fabric3JxtaException(String message) {
        super(message);
    }

}
