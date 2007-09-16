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
package org.fabric3.binding.hessian.wire;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.binding.hessian.model.physical.HessianWireSourceDefinition;
import org.fabric3.binding.hessian.model.physical.HessianWireTargetDefinition;
import org.fabric3.binding.hessian.transport.HessianServiceHandler;
import org.fabric3.binding.hessian.transport.HessianTargetInterceptor;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.WireAttachException;
import org.fabric3.spi.builder.component.WireAttacher;
import org.fabric3.spi.builder.component.WireAttacherRegistry;
import org.fabric3.spi.host.ServletHost;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.services.classloading.ClassLoaderRegistry;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Wire;

/**
 * Wire attacher for Hessian binding.
 *
 * @version $Revision$ $Date$
 */
@EagerInit
public class HessianWireAttacher implements WireAttacher<HessianWireSourceDefinition, HessianWireTargetDefinition> {
    private ClassLoaderRegistry classLoaderRegistry;
    /**
     * Servlet host.
     */
    private ServletHost servletHost;

    /**
     * Injects the wire attacher registry and servlet host.
     *
     * @param wireAttacherRegistry Wire attacher rehistry.
     * @param servletHost          Servlet host.
     * @param classLoaderRegistry  the classloader registry
     */
    public HessianWireAttacher(@Reference WireAttacherRegistry wireAttacherRegistry,
                               @Reference ServletHost servletHost,
                               @Reference ClassLoaderRegistry classLoaderRegistry) {
        wireAttacherRegistry.register(HessianWireSourceDefinition.class, this);
        wireAttacherRegistry.register(HessianWireTargetDefinition.class, this);
        this.servletHost = servletHost;
        this.classLoaderRegistry = classLoaderRegistry;
    }

    /**
     * @see org.fabric3.spi.builder.component.WireAttacher#attachToSource(org.fabric3.spi.model.physical.PhysicalWireSourceDefinition,
     *org.fabric3.spi.model.physical.PhysicalWireTargetDefinition,org.fabric3.spi.wire.Wire)
     */
    public void attachToSource(HessianWireSourceDefinition sourceDefinition,
                               PhysicalWireTargetDefinition targetDefinition,
                               Wire wire) throws WiringException {

        Map<String, Map.Entry<PhysicalOperationDefinition, InvocationChain>> ops =
                new HashMap<String, Map.Entry<PhysicalOperationDefinition, InvocationChain>>();

        for (Map.Entry<PhysicalOperationDefinition, InvocationChain> entry : wire.getInvocationChains().entrySet()) {
            ops.put(entry.getKey().getName(), entry);
        }
        URI id = sourceDefinition.getClassLoaderId();
        ClassLoader loader = classLoaderRegistry.getClassLoader(id);
        if (loader == null) {
            throw new WiringException("Classloader not found", id.toString());
        }
        HessianServiceHandler handler = new HessianServiceHandler(wire, ops, loader);
        String servicePath = sourceDefinition.getUri().getPath();
        servletHost.registerMapping(servicePath, handler);

    }

    /**
     * @see org.fabric3.spi.builder.component.WireAttacher#attachToTarget(org.fabric3.spi.model.physical.PhysicalWireSourceDefinition,
     *org.fabric3.spi.model.physical.PhysicalWireTargetDefinition,org.fabric3.spi.wire.Wire)
     */
    public void attachToTarget(PhysicalWireSourceDefinition sourceDefinition,
                               HessianWireTargetDefinition targetDefinition,
                               Wire wire) throws WiringException {

        URI uri = targetDefinition.getUri();

        try {
            for (Map.Entry<PhysicalOperationDefinition, InvocationChain> entry : wire.getInvocationChains().entrySet()) {
                PhysicalOperationDefinition op = entry.getKey();
                InvocationChain chain = entry.getValue();
                chain.addInterceptor(new HessianTargetInterceptor(uri.toURL(), op.getName()));
            }
        } catch (MalformedURLException ex) {
            throw new WireAttachException("Invalid URI", sourceDefinition.getUri(), uri, ex);
        }

    }

}
