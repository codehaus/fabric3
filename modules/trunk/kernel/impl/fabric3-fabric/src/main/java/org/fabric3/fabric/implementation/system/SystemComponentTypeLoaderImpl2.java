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
package org.fabric3.fabric.implementation.system;

import java.lang.reflect.Type;
import java.util.Set;
import java.util.Collections;

import org.osoa.sca.annotations.Reference;

import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.pojo.processor.ProcessingException;
import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.ClassWalker;
import org.fabric3.introspection.IntrospectionException;
import org.fabric3.introspection.ContractProcessor;
import org.fabric3.introspection.IntrospectionHelper;
import org.fabric3.introspection.InvalidServiceContractException;
import org.fabric3.spi.loader.LoaderException;
import org.fabric3.spi.loader.LoaderUtil;
import org.fabric3.scdl.ServiceContract;
import org.fabric3.scdl.ServiceDefinition;

/**
 * Loads a system component type
 *
 * @version $Rev$ $Date$
 */
public class SystemComponentTypeLoaderImpl2 implements SystemComponentTypeLoader {
    private final ClassWalker<SystemImplementation> classWalker;
    private final ContractProcessor contractProcessor;
    private final IntrospectionHelper helper;

    public SystemComponentTypeLoaderImpl2(@Reference ClassWalker<SystemImplementation> classWalker,
                                          @Reference ContractProcessor contractProcessor,
                                          @Reference IntrospectionHelper helper) {
        this.classWalker = classWalker;
        this.contractProcessor = contractProcessor;
        this.helper = helper;
    }

    public void load(SystemImplementation implementation, IntrospectionContext context) throws LoaderException {
        String implClassName = implementation.getImplementationClass();
        PojoComponentType componentType = new PojoComponentType(implClassName);
        implementation.setComponentType(componentType);

        ClassLoader cl = context.getTargetClassLoader();
        Class<?> implClass = LoaderUtil.loadClass(implClassName, cl);
        try {
            introspect(implementation, implClass, context);
        } catch (IntrospectionException e) {
            throw new ProcessingException(e);
        }
    }

    void introspect(SystemImplementation implementation, Class<?> implClass, IntrospectionContext context) throws IntrospectionException {
        classWalker.walk(implementation, implClass, context);

        // if no services were defined, apply heuristics
        if (implementation.getComponentType().getServices().isEmpty()) {
            serviceHeuristics(implementation, implClass, context);
        }
    }

    void serviceHeuristics(SystemImplementation implementation, Class<?> implClass, IntrospectionContext context) throws IntrospectionException {
        PojoComponentType componentType = implementation.getComponentType();
        Set<Class<?>> interfaces = helper.getImplementedInterfaces(implClass);

        // if the class does not implement any interfaces, then the class itself is the service contract
        if (interfaces.isEmpty()) {
            componentType.add(createServiceDefinition(implClass));
        } else {
            for (Class<?> serviceInterface : interfaces) {
                componentType.add(createServiceDefinition(serviceInterface));
            }
        }
    }

    ServiceDefinition createServiceDefinition(Class<?> serviceInterface) throws InvalidServiceContractException {
        ServiceContract<Type> contract = contractProcessor.introspect(serviceInterface);
        return new ServiceDefinition(contract.getInterfaceName(), contract);
    }
}