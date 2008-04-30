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
package org.fabric3.binding.aq;

/**
 * Runtime exception thrown with unexpected AQ JMS errors.
 * 
 * @version $Revision$ $Date$
 */
public class Fabric3AQException extends RuntimeException {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = -1584475278512639999L;

    /**
     * Initialises the message and the cause.
     * 
     * @param message Message for the exception.
     * @param cause Cause for the exception.
     */
    public Fabric3AQException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Initialises the message.
     * 
     * @param message Message for the exception.
     */
    public Fabric3AQException(String message) {
        super(message);
    }

}
