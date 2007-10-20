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
    public static final URI DEPLOYER_URI = URI.create(RUNTIME_NAME + "/deployer");

    public static final URI SCOPE_REGISTRY_URI = URI.create(RUNTIME_NAME + "/ScopeRegistry");

    public static final URI LOADER_URI = URI.create(RUNTIME_NAME + "/loader");

    public static final URI COMPOSITE_LOADER_URI = URI.create(RUNTIME_NAME + "/composite.componentTypeLoader");

    public static final URI DISTRIBUTED_ASSEMBLY_URI = URI.create(RUNTIME_NAME + "/distributedAssembly");

    public static final URI RUNTIME_ASSEMBLY_URI = URI.create(RUNTIME_NAME + "/RuntimeAssembly");

    public static final URI CLASSLOADER_REGISTRY_URI = URI.create(RUNTIME_NAME + "/ClassLoaderRegistry");

    public static final URI EVENT_SERVICE_URI = URI.create(RUNTIME_NAME + "/eventService");

    public static final URI WORK_SCHEDULER_URI = URI.create(RUNTIME_NAME + "/workScheduler");

    public static final URI MESSAGING_SERVICE_URI = URI.create(RUNTIME_NAME + "/MessagingService");

    public static final URI DISCOVERY_SERVICE_URI = URI.create(RUNTIME_NAME + "/DiscoveryService");

    public static final URI CONTRIBUTION_SERVICE_URI = URI.create(RUNTIME_NAME + "/ContributionService");

    public static final URI BOOT_CLASSLOADER_ID = URI.create("sca://./bootClassLoader");

    public static final URI APPLICATION_CLASSLOADER_ID = URI.create("sca://./applicationClassLoader");

    public static final URI DEFINITIONS_DEPLOYER = URI.create(RUNTIME_NAME + "/definitionsDeployer");

    public static final URI EXTENSION_CONTRIBUTION_STORE = URI.create(RUNTIME_NAME + "/ExtensionContributionStore");

    public static final URI EXTENSION_METADATA_STORE_URI = URI.create(RUNTIME_NAME + "/ExtensionMetaDataStore");


    private ComponentNames() {
    }

}
