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
package org.fabric3.spi.services.messaging;

import org.fabric3.host.Fabric3Exception;

/**
 * Checked exception thrown during discovery operations.
 *
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class MessagingException extends Fabric3Exception {

    /**
     * Initialises the exception message.
     *
     * @param message Message for the exception.
     */
    public MessagingException(String message) {
        super(message);
    }


    /**
     * Initialises the exception message.
     *
     * @param message Message for the exception.
     * @param cause   Root cause for the exception.
     */
    public MessagingException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Initialises the exception root cause.
     *
     * @param cause Root cause for the exception.
     */
    public MessagingException(Throwable cause) {
        super(cause);
    }

}
