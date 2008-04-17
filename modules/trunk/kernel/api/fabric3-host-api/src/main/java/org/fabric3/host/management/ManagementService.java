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
package org.fabric3.host.management;

import java.net.URI;

/**
 * @version $Revision$ $Date$
 */
public interface ManagementService {

    /**
     * Register a component's managed service with the management service.
     *
     * @param component           the id of the component
     * @param service             the name of the managed service
     * @param managementInterface the management interface
     * @param instance            the instance to invoke
     */
    <T> void registerService(URI component, String service, Class<T> managementInterface, T instance);
}
