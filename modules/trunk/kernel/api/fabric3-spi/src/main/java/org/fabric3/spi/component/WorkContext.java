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
package org.fabric3.spi.component;

import javax.security.auth.Subject;

import org.fabric3.scdl.Scope;

/**
 * Implementations track information associated with a request as it is processed by the runtime
 *
 * @version $Rev$ $Date$
 */
public interface WorkContext {

    /**
     * Gets the subject associated with the current invocation.
     *
     * @return Subject associated with the current invocation.
     */
    Subject getSubject();

    /**
     * Returns the identifier currently associated with the supplied scope.
     *
     * @param scope the scope whose identifier should be returned
     * @return the scope identifier
     */
    <T> T getScopeIdentifier(Scope<T> scope);

    /**
     * Sets the identifier associated with a scope.
     *
     * @param scope      the scope whose identifier we are setting
     * @param identifier the identifier for that scope
     */
    <T> void setScopeIdentifier(Scope<T> scope, T identifier);

}
