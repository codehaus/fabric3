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
package org.fabric3.junit.introspection;

import org.osoa.sca.annotations.Reference;

import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.IntrospectionException;
import org.fabric3.introspection.helper.IntrospectionHelper;
import org.fabric3.introspection.java.ClassWalker;
import org.fabric3.introspection.java.HeuristicProcessor;
import org.fabric3.introspection.helper.TypeMapping;
import org.fabric3.junit.scdl.JUnitImplementation;
import org.fabric3.loader.common.IntrospectionContextImpl;
import org.fabric3.pojo.scdl.PojoComponentType;

/**
 * @version $Rev$ $Date$
 */
public class JUnitImplementationProcessorImpl implements JUnitImplementationProcessor {
    private final ClassWalker<JUnitImplementation> classWalker;
    private final HeuristicProcessor<JUnitImplementation> heuristic;
    private final IntrospectionHelper helper;

    public JUnitImplementationProcessorImpl(@Reference(name="classWalker")ClassWalker<JUnitImplementation> classWalker,
                                       @Reference(name="heuristic")HeuristicProcessor<JUnitImplementation> heuristic,
                                       @Reference(name="helper")IntrospectionHelper helper) {
        this.classWalker = classWalker;
        this.heuristic = heuristic;
        this.helper = helper;
    }

    public void introspect(JUnitImplementation implementation, IntrospectionContext context) throws IntrospectionException {
        String implClassName = implementation.getImplementationClass();
        PojoComponentType componentType = new PojoComponentType(implClassName);
        componentType.setScope("STATELESS");
        implementation.setComponentType(componentType);

        ClassLoader cl = context.getTargetClassLoader();
        Class<?> implClass = helper.loadClass(implClassName, cl);
        TypeMapping typeMapping = helper.mapTypeParameters(implClass);

        context = new IntrospectionContextImpl(context, typeMapping);
        classWalker.walk(implementation, implClass, context);

        heuristic.applyHeuristics(implementation, implClass, context);
    }
}
