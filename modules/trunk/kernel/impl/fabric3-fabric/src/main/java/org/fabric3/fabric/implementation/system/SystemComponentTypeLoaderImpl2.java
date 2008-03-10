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

import java.util.Collection;

import org.osoa.sca.annotations.Reference;

import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.IntrospectionException;
import org.fabric3.introspection.java.ClassWalker;
import org.fabric3.introspection.java.HeuristicProcessor;
import org.fabric3.introspection.java.IntrospectionHelper;
import org.fabric3.introspection.java.TypeMapping;
import org.fabric3.introspection.xml.LoaderException;
import org.fabric3.pojo.processor.ProcessingException;
import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.loader.common.IntrospectionContextImpl;
import org.fabric3.scdl.Scope;

/**
 * Loads a system component type
 *
 * @version $Rev$ $Date$
 */
public class SystemComponentTypeLoaderImpl2 implements SystemComponentTypeLoader {
    private final ClassWalker<SystemImplementation> classWalker;
    private final Collection<? extends HeuristicProcessor<SystemImplementation>> heuristics;
    private final IntrospectionHelper helper;

    public SystemComponentTypeLoaderImpl2(@Reference ClassWalker<SystemImplementation> classWalker,
                                          @Reference Collection<? extends HeuristicProcessor<SystemImplementation>> heuristics,
                                          @Reference IntrospectionHelper helper) {
        this.classWalker = classWalker;
        this.heuristics = heuristics;
        this.helper = helper;
    }

    public void load(SystemImplementation implementation, IntrospectionContext context) throws LoaderException {
        String implClassName = implementation.getImplementationClass();
        PojoComponentType componentType = new PojoComponentType(implClassName);
        componentType.setImplementationScope(Scope.COMPOSITE);
        implementation.setComponentType(componentType);

        ClassLoader cl = context.getTargetClassLoader();
        try {
            Class<?> implClass = helper.loadClass(implClassName, cl);
            TypeMapping typeMapping = helper.mapTypeParameters(implClass);

            context = new IntrospectionContextImpl(context, typeMapping);
            classWalker.walk(implementation, implClass, context);

            for (HeuristicProcessor<SystemImplementation> heuristic : heuristics) {
                heuristic.applyHeuristics(implementation, implClass, context);
            }
        } catch (IntrospectionException e) {
            throw new ProcessingException(e);
        }
    }
}