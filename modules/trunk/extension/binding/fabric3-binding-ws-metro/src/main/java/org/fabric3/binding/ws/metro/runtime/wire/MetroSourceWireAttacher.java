/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the ñLicenseî), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an ñas isî basis,
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
package org.fabric3.binding.ws.metro.runtime.wire;

import java.net.URI;
import java.net.URL;
import java.util.Map;

import org.fabric3.binding.ws.metro.provision.MetroWireSourceDefinition;
import org.fabric3.binding.ws.metro.provision.WsdlElement;
import org.fabric3.binding.ws.metro.runtime.core.F3Invoker;
import org.fabric3.binding.ws.metro.runtime.core.MetroServlet;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.SourceWireAttacher;
import org.fabric3.spi.host.ServletHost;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.services.classloading.ClassLoaderRegistry;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Wire;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

/**
 * Source wire attacher that provisions services.
 *
 */
public class MetroSourceWireAttacher implements SourceWireAttacher<MetroWireSourceDefinition> {
    
    @Reference protected ServletHost servletHost;
    @Reference protected ClassLoaderRegistry classLoaderRegistry;
    
    private MetroServlet metroServlet = new MetroServlet();
    
    /**
     * Registers the servlet.
     */
    @Init
    public void start() {
        servletHost.registerMapping("/metro/*", metroServlet);
        
    }

    /**
     * Not supported.
     * 
     */
    public void attachObjectFactory(MetroWireSourceDefinition source, ObjectFactory<?> objectFactory, PhysicalWireTargetDefinition target) {
        throw new UnsupportedOperationException();
    }

    /**
     * Provisions the service.
     */    
    public void attachToSource(MetroWireSourceDefinition source, PhysicalWireTargetDefinition target, Wire wire) throws WiringException {
        
        try {

            URI servicePath = source.getServicePath();
            WsdlElement wsdlElement = source.getWsdlElement();
            Map<PhysicalOperationDefinition, InvocationChain> invocationChains = wire.getInvocationChains();
            URI classLoaderId = source.getClassLoaderId();
            String interfaze = source.getInterfaze();
            URL wsdlUrl = source.getWsdlUrl();
            
            ClassLoader classLoader = classLoaderRegistry.getClassLoader(classLoaderId);
            Class<?> sei = classLoader.loadClass(interfaze);
            
            F3Invoker f3Invoker = new F3Invoker(invocationChains);
            
            metroServlet.registerService(sei, wsdlUrl, "/metro" + servicePath.toASCIIString(), wsdlElement, f3Invoker);
            
        } catch (ClassNotFoundException e) {
            throw new WiringException(e);
        }
        
    }

    /**
     * Not supported.
     * 
     */
    public void detachFromSource(MetroWireSourceDefinition source, PhysicalWireTargetDefinition target) {
        throw new UnsupportedOperationException();
    }

    /**
     * Unprovisions the service.
     */ 
    public void detachObjectFactory(MetroWireSourceDefinition source, PhysicalWireTargetDefinition target) {
    }

}
