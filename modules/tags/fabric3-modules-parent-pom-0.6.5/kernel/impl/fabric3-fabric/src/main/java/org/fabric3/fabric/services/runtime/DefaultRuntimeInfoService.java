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
package org.fabric3.fabric.services.runtime;

import java.net.URI;
import java.net.URISyntaxException;

import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

import org.fabric3.host.runtime.HostInfo;
import org.fabric3.spi.model.topology.RuntimeInfo;
import org.fabric3.spi.services.advertisement.AdvertisementService;
import org.fabric3.spi.services.componentmanager.ComponentManager;
import org.fabric3.spi.services.runtime.RuntimeInfoService;

/**
 * Default implementation of the <code>RuntimeInfoService</code>. The implementation relies on the <code>AdvertisementService</code> for getting the
 * features, the <code>ComponentManager</code> for getting the list of running components and the <code>HostInfo</code> for getting the runtime id.
 *
 * @version $Revsion$ $Date$
 */
public class DefaultRuntimeInfoService implements RuntimeInfoService {
    private AdvertisementService advertService;
    private ComponentManager componentManager;
    private HostInfo hostInfo;
    private URI runtimeId;
    private String messageDestination;

    public DefaultRuntimeInfoService(@Reference(name = "advertisementService")AdvertisementService advertService,
                                     @Reference(name = "componentManager")ComponentManager componentManager,
                                     @Reference(name = "hostInfo")HostInfo hostInfo,
                                     @Property(name = "scheme", required = false)String scheme,
                                     @Property(name = "runtimeId", required = false)String runtimeId) throws URISyntaxException {
        this.advertService = advertService;
        this.componentManager = componentManager;
        this.hostInfo = hostInfo;
        if (runtimeId != null) {
            this.runtimeId = new URI(scheme, runtimeId, null);
        }
    }

    public void registerMessageDestination(String destination) {
        messageDestination = destination;
    }

    public URI getCurrentRuntimeId() {
        return runtimeId;
    }

    public RuntimeInfo getRuntimeInfo() {
        RuntimeInfo runtimeInfo = new RuntimeInfo(runtimeId);
        // add features
        runtimeInfo.setFeatures(advertService.getFeatures());
        runtimeInfo.setTransportInfo(advertService.getTransportInfo());
        // add component URIs
        for (URI componentUri : componentManager.getComponentsInHierarchy(hostInfo.getDomain())) {
            runtimeInfo.addComponent(componentUri);
        }
        runtimeInfo.setMessageDestination(messageDestination);
        return runtimeInfo;

    }

}
