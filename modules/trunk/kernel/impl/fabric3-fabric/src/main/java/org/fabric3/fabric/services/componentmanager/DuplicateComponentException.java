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
package org.fabric3.fabric.services.componentmanager;

import org.fabric3.spi.services.componentmanager.RegistrationException;

/**
 * Denotes an attempt to register a component when one is already regsitered with that id.
 *
 * @version $Rev$ $Date$
 */
public class DuplicateComponentException extends RegistrationException {
    private static final long serialVersionUID = 2257483559370700093L;

    /**
     * Constructor specifying the id of the component.
     *
     * @param message the id of the component, also the default exception message
     */
    public DuplicateComponentException(String message) {
        super(message);
    }

}
