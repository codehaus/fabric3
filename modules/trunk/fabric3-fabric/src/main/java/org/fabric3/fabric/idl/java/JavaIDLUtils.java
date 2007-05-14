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
package org.fabric3.fabric.idl.java;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

import org.fabric3.spi.model.type.Operation;
import org.fabric3.spi.model.type.DataType;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;

/**
 * Contains methods for mapping between an operation in a {@link org.fabric3.spi.model.type.ServiceContract} and a method
 * defined by a Java interface
 *
 * @version $Rev$ $Date$
 */
public final class JavaIDLUtils {

    private JavaIDLUtils() {
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
            types[i] = clazz.getClassLoader().loadClass(params.get(i));
        }
        return clazz.getMethod(name, types);
    }

    /**
     * Returns the operation matching the given method or null.
     *
     * @param method     the method to match
     * @param operations the operations to match against
     * @return a matching operation or null
     */
    public static PhysicalOperationDefinition matchPhysicalOperation(Method method,
                                                                     Collection<PhysicalOperationDefinition> operations) {
        for (PhysicalOperationDefinition operation : operations) {
            Class<?>[] params = method.getParameterTypes();
            List<String> types = operation.getParameters();
            boolean found = false;
            if (types.size() == 0 && params.length == 0) {
                return operation;
            }
            if (types.size() == params.length && method.getName().equals(operation.getName())) {
                //noinspection ForLoopReplaceableByForEach
                for (int i = 0; i < params.length; i++) {
                    if (params[i].getName().equals(types.get(0))) {
                        found = true;
                    }
                }
                if (found) {
                    return operation;
                }
            }
        }
        return null;
    }

    /**
     * Returns the physical operation matching the given method or null.
     *
     * @param method     the method to match
     * @param operations the operations to match against
     * @return a matching physical operation or null
     */
    @SuppressWarnings({"unchecked"})
    public static Operation<?> matchOperation(Method method, Collection<Operation<?>> operations) {
        Operation<?> operation = null;
        for (Operation candidate : operations) {
            // TODO support overloading
            if (candidate.getName().equals(method.getName())) {
                DataType<List<DataType<?>>> type = candidate.getInputType();
                List<DataType<?>> types = type.getLogical();
                Class<?>[] params = method.getParameterTypes();
                if (params.length != types.size()) {
                    continue;
                }
                int i = 0;
                boolean match = true;
                for (DataType<?> dataType : types) {
                    if (!dataType.getLogical().equals(params[i])) {
                        match = false;
                        break;
                    }
                    i++;
                }
                if (match) {
                    operation = candidate;
                }
                break;
            }
        }
        return operation;
    }

}
