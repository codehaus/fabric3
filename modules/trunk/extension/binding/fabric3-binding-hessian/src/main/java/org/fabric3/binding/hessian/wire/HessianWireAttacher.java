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

import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Service;

import org.fabric3.binding.hessian.model.physical.HessianWireSourceDefinition;
import org.fabric3.binding.hessian.model.physical.HessianWireTargetDefinition;
import org.fabric3.binding.hessian.transport.HessianServiceHandler;
import org.fabric3.binding.hessian.transport.HessianTargetInterceptor;
import org.fabric3.monitor.MonitorFactory;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.SourceWireAttacher;
import org.fabric3.spi.builder.component.SourceWireAttacherRegistry;
import org.fabric3.spi.builder.component.TargetWireAttacher;
import org.fabric3.spi.builder.component.TargetWireAttacherRegistry;
import org.fabric3.spi.builder.component.WireAttachException;
import org.fabric3.spi.host.ServletHost;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.services.classloading.ClassLoaderRegistry;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Wire;
import org.fabric3.spi.ObjectFactory;

import com.caucho.hessian.io.SerializerFactory;

/**
 * Wire attacher for Hessian binding.
 *
 * @version $Revision$ $Date$
 */
@EagerInit
@Service(interfaces={SourceWireAttacher.class, TargetWireAttacher.class})
public class HessianWireAttacher implements SourceWireAttacher<HessianWireSourceDefinition>, TargetWireAttacher<HessianWireTargetDefinition> {
    private final SourceWireAttacherRegistry sourceWireAttacherRegistry;
    private final TargetWireAttacherRegistry targetWireAttacherRegistry;
    private final ClassLoaderRegistry classLoaderRegistry;
    private final ServletHost servletHost;
    private final HessianWireAttacherMonitor monitor;
    private final SerializerFactory serializerFactory;

    /**
     * Injects the wire attacher registry and servlet host.
     *
     * @param sourceWireAttacherRegistry the registry for source wire attachers
     * @param targetWireAttacherRegistry the registry for target wire attachers
     * @param servletHost                Servlet host.
     * @param classLoaderRegistry        the classloader registry
     * @param monitorFactory             the monitor factory
     */
    public HessianWireAttacher(@Reference SourceWireAttacherRegistry sourceWireAttacherRegistry,
                               @Reference TargetWireAttacherRegistry targetWireAttacherRegistry,
                               @Reference ServletHost servletHost,
                               @Reference ClassLoaderRegistry classLoaderRegistry,
                               @Reference MonitorFactory monitorFactory) {
        this.sourceWireAttacherRegistry = sourceWireAttacherRegistry;
        this.targetWireAttacherRegistry = targetWireAttacherRegistry;
        this.servletHost = servletHost;
        this.classLoaderRegistry = classLoaderRegistry;
        this.monitor = monitorFactory.getMonitor(HessianWireAttacherMonitor.class);
        this.serializerFactory = new SerializerFactory();
    }

    @Init
    public void start() {
        sourceWireAttacherRegistry.register(HessianWireSourceDefinition.class, this);
        targetWireAttacherRegistry.register(HessianWireTargetDefinition.class, this);
        monitor.extensionStarted();
    }

    @Destroy
    public void stop() {
        this.monitor.extensionStopped();
        sourceWireAttacherRegistry.unregister(HessianWireSourceDefinition.class, this);
        targetWireAttacherRegistry.unregister(HessianWireTargetDefinition.class, this);
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
        HessianServiceHandler handler = new HessianServiceHandler(wire, ops, loader, serializerFactory);
        URI uri = sourceDefinition.getUri();
        String servicePath = uri.getPath();
        servletHost.registerMapping(servicePath, handler);
        monitor.provisionedEndpoint(uri);

    }

    public void attachToTarget(PhysicalWireSourceDefinition sourceDefinition,
                               HessianWireTargetDefinition targetDefinition,
                               Wire wire) throws WiringException {

        URI id = targetDefinition.getClassLoaderId();
        ClassLoader loader = classLoaderRegistry.getClassLoader(id);
        URI uri = targetDefinition.getUri();

        try {
            for (Map.Entry<PhysicalOperationDefinition, InvocationChain> entry : wire.getInvocationChains().entrySet()) {
                PhysicalOperationDefinition op = entry.getKey();
                InvocationChain chain = entry.getValue();
                chain.addInterceptor(new HessianTargetInterceptor(uri.toURL(), op.getName(), loader, serializerFactory));
            }
        } catch (MalformedURLException ex) {
            throw new WireAttachException("Invalid URI", sourceDefinition.getUri(), uri, ex);
        }

    }

    public void attachObjectFactory(HessianWireSourceDefinition source, ObjectFactory<?> objectFactory) throws WiringException {
        throw new AssertionError();
    }

    public ObjectFactory<?> createObjectFactory(HessianWireTargetDefinition target) throws WiringException {
        throw new AssertionError();
    }
}
