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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.easymock.EasyMock;
import org.fabric3.spi.ObjectFactory;

/**
 * @version $Revision$ $Date$
 */
public class MockObjectFactory<T> implements ObjectFactory<T> {

    private final Map<Class<?>, Object> mocks = new HashMap<Class<?>, Object>();
    private final T proxy;
    
    /**
     * Eager initiates the proxy.
     * 
     * @param interfaces Interfaces that need to be proxied.
     * @param classLoader Classloader for creating the dynamic proxies.
     */
    public MockObjectFactory(List<Class<?>> interfaces, ClassLoader classLoader) {
        
        for(Class<?> interfaze : interfaces) {
            mocks.put(interfaze, EasyMock.createMock(interfaze));
        }
        
        this.proxy = createProxy(interfaces, classLoader);
        
    }

    @SuppressWarnings("unchecked")
    public T getInstance() {
        return proxy;
    }

    @SuppressWarnings("unchecked")
    private T createProxy(List<Class<?>> interfaces, ClassLoader classLoader) {

        Class<?>[] mockInterfaces = new Class[interfaces.size()];
        interfaces.toArray(mockInterfaces);
        
        return (T) Proxy.newProxyInstance(classLoader, mockInterfaces, new InvocationHandler() {
            
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                
                Class<?> interfaze = method.getDeclaringClass();
                Object mock = mocks.get(interfaze);
                
                return method.invoke(mock, args);
            }
            
        });
        
    }

}
