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
package org.fabric3.itest.implementation.junit;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.osoa.sca.annotations.Constructor;
import org.osoa.sca.annotations.Reference;

import org.fabric3.pojo.instancefactory.Signature;
import org.fabric3.pojo.processor.IntrospectionRegistry;
import org.fabric3.pojo.processor.Introspector;
import org.fabric3.pojo.scdl.JavaMappedService;
import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.pojo.processor.ProcessingException;
import org.fabric3.scdl.DataType;
import org.fabric3.scdl.Operation;
import org.fabric3.scdl.Scope;
import org.fabric3.scdl.ServiceContract;
import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.spi.loader.LoaderException;
import org.fabric3.spi.loader.MissingResourceException;

/**
 * @version $Rev$ $Date$
 */
public class JUnitComponentTypeLoaderImpl implements JUnitComponentTypeLoader {
    private static final String TEST_SERVICE_NAME = "testService";
    private final Introspector introspector;

    @Constructor
    public JUnitComponentTypeLoaderImpl(@Reference IntrospectionRegistry introspector) {
        this.introspector = introspector;
    }

    public void load(ImplementationJUnit implementation, LoaderContext context) throws LoaderException {
        String className = implementation.getImplementationClass();
        Class<?> implClass;
        try {
            implClass = context.getTargetClassLoader().loadClass(className);
        } catch (ClassNotFoundException e) {
            throw new MissingResourceException(className, e);
        }
        PojoComponentType componentType = loadByIntrospection(implementation, context, implClass);
        if (componentType.getImplementationScope() == null) {
            componentType.setImplementationScope(Scope.COMPOSITE);
        }
        implementation.setComponentType(componentType);
    }

    protected PojoComponentType loadByIntrospection(ImplementationJUnit implementation,
                                                    LoaderContext loaderContext,
                                                    Class<?> implClass) throws ProcessingException {
        PojoComponentType componentType = new PojoComponentType(implClass.getName());
        introspector.introspect(implClass, componentType, loaderContext);

        if (componentType.getInitMethod() == null) {
            componentType.setInitMethod(getCallback(implClass, "setUp"));
        }
        if (componentType.getDestroyMethod() == null) {
            componentType.setDestroyMethod(getCallback(implClass, "tearDown"));
        }
        ServiceContract testContract = generateTestContract(implClass);
        JavaMappedService testService = new JavaMappedService(TEST_SERVICE_NAME, testContract);
        componentType.add(testService);
        return componentType;
    }

    protected Signature getCallback(Class<?> implClass, String name) {
        while (Object.class != implClass) {
            try {
                Method callback = implClass.getDeclaredMethod(name);
                return new Signature(callback);
            } catch (NoSuchMethodException e) {
                implClass = implClass.getSuperclass();
                continue;
            }
        }
        return null;
    }

    private static final DataType<List<DataType<Type>>> INPUT_TYPE;
    private static final DataType<Type> OUTPUT_TYPE;
    private static final List<DataType<Type>> FAULT_TYPE;

    static {
        List<DataType<Type>> paramDataTypes = Collections.emptyList();
        INPUT_TYPE = new DataType<List<DataType<Type>>>(Object[].class, paramDataTypes);
        OUTPUT_TYPE = new DataType<Type>(void.class, void.class);
        FAULT_TYPE = Collections.emptyList();
    }

    protected ServiceContract generateTestContract(Class<?> implClass) {
        List<Operation<Type>> operations = new ArrayList<Operation<Type>>();
        for (Method method : implClass.getMethods()) {
            // see if this is a test method
            if (Modifier.isStatic(method.getModifiers())) {
                continue;
            }
            if (method.getReturnType() != void.class) {
                continue;
            }
            if (method.getParameterTypes().length != 0) {
                continue;
            }
            String name = method.getName();
            if (name.length() < 5 || !name.startsWith("test")) {
                continue;
            }
            Operation<Type> operation = new Operation<Type>(name, INPUT_TYPE, OUTPUT_TYPE, FAULT_TYPE);
            operations.add(operation);
        }
        return new JUnitServiceContract(operations);
    }
}
