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
package org.fabric3.fabric.services.runtime;

import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Map;

import org.osoa.sca.annotations.Reference;

import org.fabric3.spi.model.topology.ClassLoaderResourceDescription;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.spi.component.ComponentManager;
import org.fabric3.spi.model.topology.RuntimeInfo;
import org.fabric3.spi.services.advertisement.AdvertisementService;
import org.fabric3.spi.services.classloading.ClassLoaderRegistry;
import org.fabric3.spi.services.messaging.MessageDestinationService;
import org.fabric3.spi.services.runtime.RuntimeInfoService;

/**
 * Default implementation of the <code>RuntimeInfoService</code>. The implementation relies on the
 * <code>AdvertisementService</code> for getting the features, the <code>ComponentManager</code> for getting the list of
 * running components and the <code>HostInfo</code> for getting the runtime id, and <code>ClassLoaderRegistry</code> for
 * classloader information.
 *
 * @version $Revsion$ $Date$
 */
public class DefaultRuntimeInfoService implements RuntimeInfoService {

    // Advertisement service
    private AdvertisementService advertisementService;

    // Component manager
    private ComponentManager componentManager;

    // Host info
    private HostInfo hostInfo;

    // Message destination service
    private MessageDestinationService messageDestinationService;

    private ClassLoaderRegistry classLoaderRegistry;

    public RuntimeInfo getRuntimeInfo() {
        RuntimeInfo runtimeInfo = new RuntimeInfo(hostInfo.getRuntimeId());
        // add features
        runtimeInfo.setFeatures(advertisementService.getFeatures());
        // add component URIs
        for (URI componentUri : componentManager.getComponentsInHierarchy(hostInfo.getDomain())) {
            runtimeInfo.addComponent(componentUri);
        }
        // add classloader info
        for (Map.Entry<URI, ClassLoader> entry : classLoaderRegistry.getClassLoaders().entrySet()) {
            ClassLoaderResourceDescription desc = new ClassLoaderResourceDescription(entry.getKey());
            ClassLoader loader = entry.getValue();
            desc.addParents(classLoaderRegistry.resolveParentUris(loader));
            if (loader instanceof URLClassLoader) {
                URL[] urls = ((URLClassLoader) loader).getURLs();
                desc.addClassPathUrls(Arrays.asList(urls));
            }
            runtimeInfo.addResourceDescription(desc);
        }

        // TODO Fix this in the runtime info
        String messageDestintaion = (String) messageDestinationService.getMessageDestination();
        runtimeInfo.setMessageDestination(messageDestintaion);

        return runtimeInfo;

    }

    /**
     * @param advertisementService Advertisement service to be injected.
     */
    @Reference
    public void setAdvertisementService(AdvertisementService advertisementService) {
        this.advertisementService = advertisementService;
    }

    /**
     * @param componentManager Component manager to be injected.
     */
    @Reference
    public void setComponentManager(ComponentManager componentManager) {
        this.componentManager = componentManager;
    }

    /**
     * @param hostInfo Host info to be injected.
     */
    @Reference
    public void setHostInfo(HostInfo hostInfo) {
        this.hostInfo = hostInfo;
    }

    /**
     * @param messageDestinationService Message destination service to be injected.
     */
    @Reference
    public void setMessageDestinationService(MessageDestinationService messageDestinationService) {
        this.messageDestinationService = messageDestinationService;
    }

    @Reference
    public void setClassLoaderRegistry(ClassLoaderRegistry classLoaderRegistry) {
        this.classLoaderRegistry = classLoaderRegistry;
    }

}
