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

package org.fabric3.binding.ws.wire;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.apache.cxf.frontend.ClientProxyFactoryBean;
import org.apache.cxf.frontend.ServerFactoryBean;
import org.fabric3.binding.ws.model.physical.WsWireSourceDefinition;
import org.fabric3.binding.ws.model.physical.WsWireTargetDefinition;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.WireAttacher;
import org.fabric3.spi.builder.component.WireAttacherRegistry;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Wire;
import org.osoa.sca.annotations.Reference;

/**
 * Wire attacher for web services.
 * 
 * @version $Revision$ $Date$
 */
public class WsWireAttacher implements WireAttacher<WsWireSourceDefinition, WsWireTargetDefinition> {

    /**
     * Injects the wire attacher registry and servlet host.
     * 
     * @param wireAttacherRegistry Wire attacher registry.
     */
    public WsWireAttacher(@Reference WireAttacherRegistry wireAttacherRegistry) {
        wireAttacherRegistry.register(WsWireSourceDefinition.class, this);
        wireAttacherRegistry.register(WsWireTargetDefinition.class, this);
    }

    /**
     * @see org.fabric3.spi.builder.component.WireAttacher#attachToSource(org.fabric3.spi.model.physical.PhysicalWireSourceDefinition, org.fabric3.spi.model.physical.PhysicalWireTargetDefinition, org.fabric3.spi.wire.Wire)
     */
    public void attachToSource(WsWireSourceDefinition sourceDefinition,
                               PhysicalWireTargetDefinition targetDefinition,
                               Wire wire) throws WiringException {

        Map<String, Interceptor> headInterceptors = new HashMap<String, Interceptor>();

        for (Map.Entry<PhysicalOperationDefinition, InvocationChain> entry : wire.getInvocationChains().entrySet()) {
            headInterceptors.put(entry.getKey().getName(), entry.getValue().getHeadInterceptor());
        }
        
        Class<?> service = sourceDefinition.getServiceInterface();
        Object implementor = ServiceProxyHandler.newInstance(service, headInterceptors, wire);
        
        ServerFactoryBean serverFactory = new ServerFactoryBean();
        serverFactory.setAddress(sourceDefinition.getUri().toASCIIString());
        serverFactory.setServiceBean(implementor);

    }

    /**
     * @see org.fabric3.spi.builder.component.WireAttacher#attachToTarget(org.fabric3.spi.model.physical.PhysicalWireSourceDefinition, org.fabric3.spi.model.physical.PhysicalWireTargetDefinition, org.fabric3.spi.wire.Wire)
     */
    public void attachToTarget(PhysicalWireSourceDefinition sourceDefinition,
                               WsWireTargetDefinition targetDefinition,
                               Wire wire) throws WiringException {
        
        Class<?> referenceClass = targetDefinition.getReferenceInterface();
        
        ClientProxyFactoryBean factory = new ClientProxyFactoryBean();
        factory.setServiceClass(referenceClass);
        factory.setAddress(targetDefinition.getUri().toASCIIString());
        
        Object proxy = factory.create();
        
        for(Method method : referenceClass.getDeclaredMethods()) {
            for (Map.Entry<PhysicalOperationDefinition, InvocationChain> entry : wire.getInvocationChains().entrySet()) {
                PhysicalOperationDefinition op = entry.getKey();
                InvocationChain chain = entry.getValue();
                if(method.getName().equals(op.getName())) {
                    chain.addInterceptor(new WsTargetInterceptor(method, proxy));
                }
            }
        }
        
    }

}
