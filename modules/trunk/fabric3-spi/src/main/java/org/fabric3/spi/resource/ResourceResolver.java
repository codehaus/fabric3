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
package org.fabric3.spi.resource;

import org.fabric3.spi.model.instance.LogicalComponent;

/**
 *
 * Abstraction for resolving resource references.
 * 
 * @version $Revision$ $Date$
 */
public interface ResourceResolver {
    
    /**
     * @param component Component whose resources need to be resolved.
     * @param domain Domain within which the target is searchd for.
     * @throws ResourceResolutionException If the resource cannot be resolved.
     */
    void resolve(LogicalComponent<?> component, LogicalComponent<?> domain) throws ResourceResolutionException;

}
