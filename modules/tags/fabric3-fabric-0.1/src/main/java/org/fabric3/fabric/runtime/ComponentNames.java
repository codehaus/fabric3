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
package org.fabric3.fabric.runtime;

import java.net.URI;

/**
 * Class that defines the URIs of well-known component
 *
 * @version $Rev$ $Date$
 */
public final class ComponentNames {

    /**
     * The name of the component that is the root of the system composite tree.
     */
    public static final String RUNTIME_NAME = "fabric3://./runtime";

    public static final URI RUNTIME_URI = URI.create(RUNTIME_NAME);

    /**
     * The name of the component that contains the deployer.
     */
    public static final URI DEPLOYER_URI = URI.create(RUNTIME_NAME + "/main/deployer");

    public static final URI SCOPE_REGISTRY_URI = URI.create(RUNTIME_NAME + "/main/ScopeRegistry");

    public static final URI LOADER_URI = URI.create(RUNTIME_NAME + "/main/loader");

    public static final URI COMPOSITE_LOADER_URI = URI.create(RUNTIME_NAME + "/main/composite.componentTypeLoader");

    public static final URI DISTRIBUTED_ASSEMBLY_URI = URI.create(RUNTIME_NAME + "/main/distributedAssembly");

    public static final URI RUNTIME_ASSEMBLY_URI = URI.create(RUNTIME_NAME + "/main/RuntimeAssembly");

    public static final URI CLASSLOADER_REGISTRY_URI = URI.create(RUNTIME_NAME + "/main/ClassLoaderRegistry");

    public static final URI EVENT_SERVICE_URI = URI.create(RUNTIME_NAME + "/main/eventService");

    public static final URI WORK_SCHEDULER_URI = URI.create(RUNTIME_NAME + "/main/workScheduler");

    public static final URI MESSAGING_SERVICE_URI = URI.create(RUNTIME_NAME + "/main/MessagingService");

    public static final URI DISCOVERY_SERVICE_URI = URI.create(RUNTIME_NAME + "/main/DiscoveryService");

    private ComponentNames() {
    }

}
