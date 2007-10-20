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

package org.fabric3.idl.wsdl.version;

import org.fabric3.host.Fabric3RuntimeException;

/**
 * Exception thrown in checking wsdl version.
 * 
 * @version $Revison$ $Date$
 */
public class WsdlVersionCheckerException extends Fabric3RuntimeException {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 7401444222777756374L;

    /**
     * @param message Exception message.
     * @param cause Exception cause.
     */
    public WsdlVersionCheckerException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message Exception message.
     */
    public WsdlVersionCheckerException(String message) {
        super(message);
    }

}
