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
import java.util.Map;

import com.caucho.hessian.io.SerializerFactory;
import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.api.annotation.Monitor;
import org.fabric3.binding.hessian.model.physical.HessianWireTargetDefinition;
import org.fabric3.binding.hessian.transport.HessianTargetInterceptor;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.TargetWireAttacher;
import org.fabric3.spi.builder.component.WireAttachException;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.services.classloading.ClassLoaderRegistry;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Wire;

/**
 * Wire attacher for Hessian binding.
 *
 * @version $Revision$ $Date$
 */
@EagerInit
public class HessianTargetWireAttacher implements TargetWireAttacher<HessianWireTargetDefinition> {
    private final ClassLoaderRegistry classLoaderRegistry;
    private final HessianWireAttacherMonitor monitor;
    private final SerializerFactory serializerFactory;

    public HessianTargetWireAttacher(@Reference ClassLoaderRegistry classLoaderRegistry,
                                     @Monitor HessianWireAttacherMonitor monitor) {
        this.classLoaderRegistry = classLoaderRegistry;
        this.monitor = monitor;
        this.serializerFactory = new SerializerFactory();
    }

    @Init
    public void start() {
        monitor.extensionStarted();
    }

    @Destroy
    public void stop() {
        this.monitor.extensionStopped();
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

    public ObjectFactory<?> createObjectFactory(HessianWireTargetDefinition target) throws WiringException {
        throw new AssertionError();
    }
}