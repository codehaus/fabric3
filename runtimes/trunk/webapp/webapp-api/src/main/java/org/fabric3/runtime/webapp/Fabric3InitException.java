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
package org.fabric3.runtime.webapp;

import org.fabric3.host.Fabric3RuntimeException;

/**
 * Exception indicating that there was a problem starting the Fabric3 runtime.
 *
 * @version $Rev$ $Date$
 */
public class Fabric3InitException extends Fabric3RuntimeException {
    public Fabric3InitException(String string) {
        super(string);
    }

    public Fabric3InitException(String message, String identifier) {
        super(message, identifier);
    }

    public Fabric3InitException(String string, Throwable throwable) {
        super(string, throwable);
    }

    public Fabric3InitException(String message, String identifier, Throwable cause) {
        super(message, identifier, cause);
    }

    public Fabric3InitException(Throwable throwable) {
        super(throwable);
    }
}
