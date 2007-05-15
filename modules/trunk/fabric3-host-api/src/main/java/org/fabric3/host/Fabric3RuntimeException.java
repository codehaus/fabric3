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
package org.fabric3.host;

import java.io.PrintWriter;

/**
 * The root unchecked exception for the fabric3 runtime.
 *
 * @version $Rev$ $Date$
 */

public abstract class Fabric3RuntimeException extends RuntimeException {
    private static final long serialVersionUID = -759677431966121786L;
    private final String identifier;

    /**
     * Override constructor from RuntimeException.
     *
     * @see RuntimeException
     */
    public Fabric3RuntimeException() {
        super();
        this.identifier = null;
    }

    /**
     * Override constructor from RuntimeException.
     *
     * @param message passed to RuntimeException
     * @see RuntimeException
     */
    public Fabric3RuntimeException(String message) {
        super(message);
        this.identifier = null;
    }


    /**
     * Override constructor from Exception.
     *
     * @param message    passed to Exception
     * @param identifier additional error information referred to in the error message
     * @see Exception
     */
    protected Fabric3RuntimeException(String message, String identifier) {
        super(message);
        this.identifier = identifier;
    }

    /**
     * Override constructor from RuntimeException.
     *
     * @param message passed to RuntimeException
     * @param cause   passed to RuntimeException
     * @see RuntimeException
     */
    public Fabric3RuntimeException(String message, Throwable cause) {
        super(message, cause);
        this.identifier = null;
    }


    /**
     * Override constructor from Exception.
     *
     * @param message    passed to Exception
     * @param identifier additional error information referred to in the error message
     * @param cause      passed to RuntimeException
     * @see Exception
     */
    protected Fabric3RuntimeException(String message, String identifier, Throwable cause) {
        super(message, cause);
        this.identifier = identifier;
    }

    /**
     * Override constructor from RuntimeException.
     *
     * @param cause passed to RuntimeException
     * @see RuntimeException
     */
    public Fabric3RuntimeException(Throwable cause) {
        super(cause);
        this.identifier = null;
    }

    /**
     * Returns a string representing additional error information referred to in the error message.
     *
     * @return additional error information
     */
    public String getIdentifier() {
        return identifier;
    }

    public PrintWriter appendBaseMessage(PrintWriter writer) {
        if (identifier == null) {
            if (super.getMessage() == null) {
                return writer;
            }
            return writer.append(super.getMessage());
        }
        if (super.getMessage() != null) {
            writer.append(super.getMessage());
        }
        writer.append(" [").append(identifier).append(']');
        return writer;
    }

}
