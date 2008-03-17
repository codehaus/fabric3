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
package org.fabric3.binding.hessian.runtime;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import com.caucho.hessian.io.SerializerFactory;
import org.osoa.sca.annotations.Reference;

import org.fabric3.api.annotation.Monitor;
import org.fabric3.binding.hessian.provision.HessianWireSourceDefinition;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.SourceWireAttacher;
import org.fabric3.spi.host.ServletHost;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.services.classloading.ClassLoaderRegistry;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Wire;

/**
 * Wire attacher for Hessian binding.
 *
 * @version $Revision$ $Date$
 */
public class HessianSourceWireAttacher implements SourceWireAttacher<HessianWireSourceDefinition> {
    private final ClassLoaderRegistry classLoaderRegistry;
    private final ServletHost servletHost;
    private final HessianWireAttacherMonitor monitor;
    private final SerializerFactory serializerFactory;

    /**
     * Injects the wire attacher registry and servlet host.
     *
     * @param servletHost                Servlet host.
     * @param classLoaderRegistry        the classloader registry
     * @param monitor                    the Hessian monitor
     */
    public HessianSourceWireAttacher(@Reference ServletHost servletHost,
                                     @Reference ClassLoaderRegistry classLoaderRegistry,
                                     @Monitor HessianWireAttacherMonitor monitor) {
        this.servletHost = servletHost;
        this.classLoaderRegistry = classLoaderRegistry;
        this.monitor = monitor;
        this.serializerFactory = new SerializerFactory();
    }

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
        String callbackUri = null;
        if (targetDefinition.getCallbackUri() != null) {
            callbackUri = targetDefinition.getCallbackUri().toString();
        }
        HessianServiceHandler handler = new HessianServiceHandler(ops, callbackUri, loader, serializerFactory);
        URI uri = sourceDefinition.getUri();
        String servicePath = uri.getPath();
        servletHost.registerMapping(servicePath, handler);
        monitor.provisionedEndpoint(uri);

    }

    public void attachObjectFactory(HessianWireSourceDefinition source, ObjectFactory<?> objectFactory) throws WiringException {
        throw new AssertionError();
    }
}
