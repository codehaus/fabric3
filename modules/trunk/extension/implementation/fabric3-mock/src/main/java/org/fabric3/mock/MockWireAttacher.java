/*
 * See the NOTICE file distributed with this work for information
 * regarding copyright ownership.  This file is licensed
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
package org.fabric3.mock;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Map;

import org.easymock.IMocksControl;
import org.fabric3.spi.builder.component.WireAttachException;
import org.fabric3.spi.builder.component.WireAttacher;
import org.fabric3.spi.builder.component.WireAttacherRegistry;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.services.classloading.ClassLoaderRegistry;
import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Message;
import org.fabric3.spi.wire.MessageImpl;
import org.fabric3.spi.wire.Wire;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

/**
 * @version $Revision$ $Date$
 */
@EagerInit
public class MockWireAttacher implements WireAttacher<PhysicalWireSourceDefinition, MockWireTargetDefinition> {
    
    private static final URI CLASS_LOADER_ID = URI.create("sca://./applicationClassLoader");
    
    private final WireAttacherRegistry wireAttacherRegistry;
    private final ClassLoaderRegistry classLoaderRegistry;
    private final IMocksControl control;
    
    public MockWireAttacher(@Reference WireAttacherRegistry wireAttacherRegistry,
                            @Reference ClassLoaderRegistry classLoaderRegistry,
                            @Reference IMocksControl control) {
        this.wireAttacherRegistry = wireAttacherRegistry;
        this.classLoaderRegistry = classLoaderRegistry;
        this.control = control;
    }
    
    @Init
    public void init() {
        wireAttacherRegistry.register(MockWireTargetDefinition.class, this);
    }

    public void attachToSource(PhysicalWireSourceDefinition wireSourceDefinition, 
                               PhysicalWireTargetDefinition wireTargetDefinition, 
                               Wire wire) {
        throw new UnsupportedOperationException("Mock components cant be sources for wires");
    }

    public void attachToTarget(PhysicalWireSourceDefinition wireSourceDefinition, 
                               MockWireTargetDefinition wireTargetDefinition, 
                               Wire wire) throws WireAttachException {
        
        if(wireSourceDefinition.isOptimizable()) {
            return;
        }
        
        ClassLoader classLoader = classLoaderRegistry.getClassLoader(CLASS_LOADER_ID);
        
        String interfaceClass = wireTargetDefinition.getMockedInterface();
        
        try {
            
            Class<?> mockedInterface = classLoader.loadClass(interfaceClass);
            Object mock = control.createMock(mockedInterface);
            
            for (Map.Entry<PhysicalOperationDefinition, InvocationChain> entry : wire.getInvocationChains().entrySet()) {
                PhysicalOperationDefinition op = entry.getKey();
                InvocationChain chain = entry.getValue();
                
                for(Method method : mockedInterface.getDeclaredMethods()) {
                    if(op.getName().equals(method.getName())) {
                        chain.addInterceptor(new MockTargetInterceptor(mock, method));
                    }
                }
            }
            
        } catch (ClassNotFoundException e) {
            URI sourceUri = wireSourceDefinition.getUri();
            URI targetUri = wireTargetDefinition.getUri();
            throw new WireAttachException("Unable to load interface " + interfaceClass, sourceUri, targetUri, e);
        }
        
    }
    
    private class MockTargetInterceptor implements Interceptor {
        
        private Interceptor next;
        private Object mock;
        private Method method;
        
        private MockTargetInterceptor(Object mock, Method method) {
            this.mock = mock;
            this.method = method;
        }

        public Interceptor getNext() {
            return next;
        }

        public Message invoke(Message message) {
            
            try {
                
                Object[] args = (Object[]) message.getBody();
                Object ret = method.invoke(mock, args);
                Message out = new MessageImpl();
                out.setBody(ret);
                
                return out;
                
            } catch (IllegalAccessException e) {
                throw new AssertionError(e);
            } catch (InvocationTargetException e) {
                throw new AssertionError(e);
            }
            
        }

        public void setNext(Interceptor next) {
            this.next = next;
        }
        
    }

}
