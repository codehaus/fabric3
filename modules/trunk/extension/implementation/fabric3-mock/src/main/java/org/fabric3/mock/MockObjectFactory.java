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
import org.fabric3.spi.ObjectCreationException;
import org.fabric3.spi.ObjectFactory;

/**
 * @version $Revision$ $Date$
 */
public class MockObjectFactory<T> implements ObjectFactory<T> {
    
    private final Class<?>[] interfaces;
    private final ClassLoader classLoader;
    private final Map<Class<?>, Object> mocks = new HashMap<Class<?>, Object>();
    
    public MockObjectFactory(List<Class<?>> interfaces, ClassLoader classLoader) {
        
        this.interfaces = new Class<?>[interfaces.size()];
        interfaces.toArray(this.interfaces);
        
        for(Class<?> interfaze : interfaces) {
            mocks.put(interfaze, EasyMock.createMock(interfaze));
        }

        this.classLoader = classLoader;
        
    }

    @SuppressWarnings("unchecked")
    public T getInstance() throws ObjectCreationException {
        
        return (T) Proxy.newProxyInstance(classLoader, interfaces, new InvocationHandler() {
            
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                
                Class<?> interfaze = method.getDeclaringClass();
                Object mock = mocks.get(interfaze);
                
                return method.invoke(mock, args);
            }
            
        });
        
    }

}
