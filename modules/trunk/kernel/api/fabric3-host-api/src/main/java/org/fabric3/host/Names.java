/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
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
package org.fabric3.host;

import java.net.URI;

/**
 * Defines URIs of well-known runtime components and classloaders available through the host API.
 *
 * @version $Rev$ $Date$
 */
public interface Names {

    URI BOOT_CLASSLOADER_ID = URI.create("Fabric3BootClassLoader");

    URI HOST_CLASSLOADER_ID = URI.create("Fabric3HostClassLoader");

    String RUNTIME_NAME = "fabric3://runtime";

    URI RUNTIME_URI = URI.create(RUNTIME_NAME);

    URI APPLICATION_DOMAIN_URI = URI.create(RUNTIME_NAME + "/ApplicationDomain");

    URI CONTRIBUTION_SERVICE_URI = URI.create(RUNTIME_NAME + "/ContributionService");

    URI RUNTIME_DOMAIN_SERVICE_URI = URI.create(RUNTIME_NAME + "/RuntimeDomain");

}
