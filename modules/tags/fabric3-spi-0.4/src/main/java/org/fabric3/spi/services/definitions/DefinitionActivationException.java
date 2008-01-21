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
package org.fabric3.spi.services.definitions;

import org.fabric3.host.Fabric3Exception;

/**
 * Execption thrown if unable to activate definition.
 * 
 * @version $Revision$ $Date$
 */
public class DefinitionActivationException extends Fabric3Exception {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = -7879956099570998326L;

    /**
     * @param message Message for the exception.
     * @param identifier Contextual information.
     */
    public DefinitionActivationException(String message, String identifier) {
        super(message, identifier);
    }

}
