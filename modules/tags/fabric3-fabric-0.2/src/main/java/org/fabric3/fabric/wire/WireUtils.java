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
package org.fabric3.fabric.wire;

import org.fabric3.fabric.services.classloading.ClassLoaderRegistryImpl;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.ProxyCreationException;
import org.fabric3.spi.wire.Wire;
import org.fabric3.spi.services.classloading.ClassLoaderRegistry;

/**
 * Utilities for operating on wires
 *
 * @version $Rev$ $Date$
 */
public final class WireUtils {// TODO This is a hack for the demo
private static final ClassLoaderRegistry registry = new ClassLoaderRegistryImpl();

    private WireUtils() {
    }

    public static Map<Method, InvocationChain> createInterfaceToWireMapping(Class<?> interfaze, Wire wire)
            throws NoMethodForOperationException {
        Map<PhysicalOperationDefinition, InvocationChain> invocationChains = wire.getInvocationChains();

        Map<Method, InvocationChain> chains = new HashMap<Method, InvocationChain>(invocationChains.size());
        for (Map.Entry<PhysicalOperationDefinition, InvocationChain> entry : invocationChains.entrySet()) {
            PhysicalOperationDefinition operation = entry.getKey();
            try {
                Method method = findMethod(interfaze, operation);
                chains.put(method, entry.getValue());
            } catch (NoSuchMethodException e) {
                throw new NoMethodForOperationException(operation.getName());
            } catch (ClassNotFoundException e) {
                throw new ProxyCreationException(e);
            }
        }
        return chains;
    }

    /**
     * Returns the matching method from the class for a given operation.
     *
     * @param clazz     the class to introspect
     * @param operation the operation to match
     * @return a matching method
     * @throws NoSuchMethodException  if a matching method is not found
     * @throws ClassNotFoundException if a parameter type specified in the operation is not found
     */
    public static Method findMethod(Class<?> clazz, PhysicalOperationDefinition operation)
            throws NoSuchMethodException, ClassNotFoundException {
        String name = operation.getName();
        List<String> params = operation.getParameters();
        Class<?>[] types = new Class<?>[params.size()];
        for (int i = 0; i < params.size(); i++) {
            types[i] = registry.loadClass(clazz.getClassLoader(), params.get(i));
        }
        return clazz.getMethod(name, types);
    }
}
