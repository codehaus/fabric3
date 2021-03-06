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
package org.fabric3.jmx;

import org.fabric3.host.Fabric3RuntimeException;

/**
 * Maps JMX exceptions to runtime exceptions.
 * 
 * @version $Revision$ $Date$
 */
public class JmxException extends Fabric3RuntimeException {

	private static final long serialVersionUID = -37382269762178444L;

	/**
     * Initializes the root cause.
     * @param cause Initializes the root cause.
     */
    public JmxException(Throwable cause) {
        super(cause);
    }

    /**
     * Initializes the root cause.
     * @param message Message for the exception.
     * @param cause Initializes the root cause.
     */
    public JmxException(String message, Throwable cause) {
        super(message, cause);
    }

}
