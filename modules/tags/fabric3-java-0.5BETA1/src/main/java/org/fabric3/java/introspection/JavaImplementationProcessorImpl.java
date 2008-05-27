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
package org.fabric3.java.introspection;

import java.net.URL;

import org.osoa.sca.annotations.Reference;

import org.fabric3.introspection.DefaultIntrospectionContext;
import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.IntrospectionHelper;
import org.fabric3.introspection.TypeMapping;
import org.fabric3.introspection.java.ClassWalker;
import org.fabric3.introspection.java.HeuristicProcessor;
import org.fabric3.introspection.java.ImplementationNotFoundException;
import org.fabric3.introspection.xml.LoaderException;
import org.fabric3.java.scdl.JavaImplementation;
import org.fabric3.pojo.scdl.PojoComponentType;

/**
 * @version $Rev$ $Date$
 */
public class JavaImplementationProcessorImpl implements JavaImplementationProcessor {
    private final ClassWalker<JavaImplementation> classWalker;
    private final HeuristicProcessor<JavaImplementation> heuristic;
    private final IntrospectionHelper helper;

    public JavaImplementationProcessorImpl(@Reference(name = "classWalker")ClassWalker<JavaImplementation> classWalker,
                                           @Reference(name = "heuristic")HeuristicProcessor<JavaImplementation> heuristic,
                                           @Reference(name = "helper")IntrospectionHelper helper) {
        this.classWalker = classWalker;
        this.heuristic = heuristic;
        this.helper = helper;
    }

    public void introspect(JavaImplementation implementation, IntrospectionContext context) {
        String implClassName = implementation.getImplementationClass();
        PojoComponentType componentType = new PojoComponentType(implClassName);
        componentType.setScope("STATELESS");
        implementation.setComponentType(componentType);

        ClassLoader cl = context.getTargetClassLoader();

        Class<?> implClass;
        try {
            implClass = helper.loadClass(implClassName, cl);
        } catch (ImplementationNotFoundException e) {
            context.addError(new ImplementationNotFound(implementation));
            return;
        }
        TypeMapping typeMapping = helper.mapTypeParameters(implClass);

        IntrospectionContext newContext = new DefaultIntrospectionContext(context, typeMapping);
        classWalker.walk(implementation, implClass, newContext);

        heuristic.applyHeuristics(implementation, implClass, newContext);
        if (newContext.hasErrors()) {
            context.addErrors(newContext.getErrors());
        }
        if (newContext.hasWarnings()) {
            context.addWarnings(newContext.getWarnings());
        }

    }

    PojoComponentType loadFromSidefile(URL url, IntrospectionContext introspectionContext) throws LoaderException {
        // FIXME we need to merge the loaded componentType information with the introspection result
        throw new UnsupportedOperationException();
/*
        PojoComponentType componentType = new PojoComponentType();
        return loader.load(url, PojoComponentType.class, introspectionContext);
*/
    }
}
