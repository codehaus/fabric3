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
package org.fabric3.system.introspection;

import org.osoa.sca.annotations.Reference;

import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.IntrospectionException;
import org.fabric3.introspection.IntrospectionHelper;
import org.fabric3.introspection.java.ClassWalker;
import org.fabric3.introspection.java.HeuristicProcessor;
import org.fabric3.introspection.TypeMapping;
import org.fabric3.introspection.DefaultIntrospectionContext;
import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.system.scdl.SystemImplementation;

/**
 * Loads a system component type
 *
 * @version $Rev$ $Date$
 */
public class SystemImplementationProcessorImpl implements SystemImplementationProcessor {
    private final ClassWalker<SystemImplementation> classWalker;
    private final HeuristicProcessor<SystemImplementation> heuristic;
    private final IntrospectionHelper helper;

    public SystemImplementationProcessorImpl(@Reference(name = "classWalker")ClassWalker<SystemImplementation> classWalker,
                                             @Reference(name = "heuristic")HeuristicProcessor<SystemImplementation> heuristic,
                                             @Reference(name = "helper") IntrospectionHelper helper) {
        this.classWalker = classWalker;
        this.heuristic = heuristic;
        this.helper = helper;
    }

    public void introspect(SystemImplementation implementation, IntrospectionContext context) throws IntrospectionException {
        String implClassName = implementation.getImplementationClass();
        PojoComponentType componentType = new PojoComponentType(implClassName);
        componentType.setScope("COMPOSITE");
        implementation.setComponentType(componentType);

        ClassLoader cl = context.getTargetClassLoader();
        Class<?> implClass = helper.loadClass(implClassName, cl);
        TypeMapping typeMapping = helper.mapTypeParameters(implClass);

        context = new DefaultIntrospectionContext(context, typeMapping);
        classWalker.walk(implementation, implClass, context);

        heuristic.applyHeuristics(implementation, implClass, context);
    }
}