/*
 * Fabric3
 * Copyright � 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the �License�), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an �as is� basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 * --- Original Apache License ---
 *
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
 * Class that defines the URIs of well-known components.
 *
 * @version $Rev$ $Date$
 */
public final class ComponentNames {

    public static final String RUNTIME_NAME = "fabric3://runtime";

    public static final URI RUNTIME_URI = URI.create(RUNTIME_NAME);

    public static final URI BOOT_CLASSLOADER_ID = URI.create("fabric3://runtime/BootClassLoader");

    public static final URI APPLICATION_CLASSLOADER_ID = URI.create("fabric3://runtime/ApplicationClassLoader");

    public static final URI APPLICATION_DOMAIN_URI = URI.create(RUNTIME_NAME + "/ApplicationDomain");

    public static final URI RUNTIME_DOMAIN_URI = URI.create(RUNTIME_NAME + "/RuntimeDomain");

    public static final URI EVENT_SERVICE_URI = URI.create(RUNTIME_NAME + "/EventService");

    public static final URI DISCOVERY_SERVICE_URI = URI.create(RUNTIME_NAME + "/DiscoveryService");

    public static final URI CONTRIBUTION_SERVICE_URI = URI.create(RUNTIME_NAME + "/ContributionService");

    public static final URI DEFINITIONS_REGISTRY = URI.create(RUNTIME_NAME + "/DefinitionsRegistry");

    public static final URI METADATA_STORE_URI = URI.create(RUNTIME_NAME + "/MetaDataStore");

    public static final URI XML_MANIFEST_PROCESSOR = URI.create(RUNTIME_NAME + "/XmlManifestProcessor");

    public static final URI XML_FACTORY_URI = URI.create(RUNTIME_NAME + "/XMLFactory");

    private ComponentNames() {
    }

}
