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
package org.fabric3.runtime.standalone.host.implementation.launched;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.osoa.sca.annotations.Reference;

import org.fabric3.pojo.processor.IntrospectionRegistry;
import org.fabric3.pojo.processor.Introspector;
import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.pojo.processor.ProcessingException;
import org.fabric3.scdl.DataType;
import org.fabric3.scdl.Operation;
import org.fabric3.scdl.Scope;
import org.fabric3.scdl.ServiceContract;
import org.fabric3.scdl.ServiceDefinition;
import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.spi.loader.LoaderException;
import org.fabric3.spi.loader.MissingResourceException;

/**
 * @version $Revision$ $Date$
 */
public class LaunchedComponentTypeLoaderImpl implements LaunchedComponentTypeLoader {
    private static final String SERVICE_NAME = "main";
    private final Introspector introspector;

    public LaunchedComponentTypeLoaderImpl(@Reference IntrospectionRegistry introspector) {
        this.introspector = introspector;
    }

    public void load(Launched implementation, IntrospectionContext introspectionContext) throws LoaderException {
        String className = implementation.getImplementationClass();
        Class<?> implClass;
        try {
            implClass = introspectionContext.getTargetClassLoader().loadClass(className);
        } catch (ClassNotFoundException e) {
            throw new MissingResourceException(className, e);
        }
        PojoComponentType componentType = loadByIntrospection(implementation, introspectionContext, implClass);
        componentType.setImplementationScope(Scope.COMPOSITE);
        implementation.setComponentType(componentType);
    }

    protected PojoComponentType loadByIntrospection(
            Launched implementation,
            IntrospectionContext introspectionContext,
            Class<?> implClass) throws ProcessingException {
        PojoComponentType componentType = new PojoComponentType(implClass.getName());
        introspector.introspect(implClass, componentType, introspectionContext);

        ServiceContract launchedContract = generateContract(implClass);
        ServiceDefinition testService = new ServiceDefinition(SERVICE_NAME, launchedContract);
        componentType.add(testService);
        return componentType;
    }

    private static final DataType<List<DataType<Type>>> INPUT_TYPE;
    private static final DataType<Type> OUTPUT_TYPE;
    private static final List<DataType<Type>> FAULT_TYPE;

    static {
        List<DataType<Type>> paramDataTypes = new ArrayList<DataType<Type>>();
        //noinspection unchecked
        paramDataTypes.add(new DataType(String[].class, String[].class));
        INPUT_TYPE = new DataType<List<DataType<Type>>>(Object[].class, paramDataTypes);
        OUTPUT_TYPE = new DataType<Type>(Object.class, Object.class);
        FAULT_TYPE = Collections.emptyList();
    }

    protected ServiceContract generateContract(Class<?> implClass) {
        List<Operation<Type>> operations = new ArrayList<Operation<Type>>();
        Operation<Type> operation = new Operation<Type>("main", INPUT_TYPE, OUTPUT_TYPE, FAULT_TYPE);
        operations.add(operation);
        return new LaunchedServiceContract(operations);
    }

}
