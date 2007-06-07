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
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.osoa.sca.annotations.Reference;

import org.fabric3.spi.implementation.java.IntrospectionRegistry;
import org.fabric3.spi.implementation.java.Introspector;
import org.fabric3.spi.implementation.java.JavaMappedService;
import org.fabric3.spi.implementation.java.PojoComponentType;
import org.fabric3.spi.implementation.java.ProcessingException;
import org.fabric3.spi.loader.ComponentTypeLoader;
import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.spi.loader.LoaderException;
import org.fabric3.spi.loader.MissingResourceException;
import org.fabric3.spi.model.type.DataType;
import org.fabric3.spi.model.type.Operation;
import org.fabric3.spi.model.type.Scope;
import org.fabric3.spi.model.type.ServiceContract;

/**
 * @version $Revision$ $Date$
 */
public class LaunchedComponentTypeLoader implements ComponentTypeLoader<Launched> {
    private static final URI SERVICE_NAME = URI.create("#main");
    private final Introspector introspector;

    public LaunchedComponentTypeLoader(@Reference IntrospectionRegistry introspector) {
        this.introspector = introspector;
    }

    public void load(Launched implementation, LoaderContext loaderContext) throws LoaderException {
        String className = implementation.getClassName();
        Class<?> implClass;
        try {
            implClass = loaderContext.getClassLoader().loadClass(className);
        } catch (ClassNotFoundException e) {
            throw new MissingResourceException(className, e);
        }
        PojoComponentType componentType = loadByIntrospection(implementation, loaderContext, implClass);
        componentType.setImplementationScope(Scope.COMPOSITE);
        implementation.setComponentType(componentType);
    }

    protected PojoComponentType loadByIntrospection(
            Launched implementation,
            LoaderContext loaderContext,
            Class<?> implClass) throws ProcessingException {
        PojoComponentType componentType =
                new PojoComponentType(implClass);
        introspector.introspect(implClass, componentType, loaderContext);

        ServiceContract launchedContract = generateContract(implClass);
        JavaMappedService testService = new JavaMappedService(SERVICE_NAME, launchedContract, false);
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
