/*
 * See the NOTICE file distributed with this work for information
 * regarding copyright ownership.  This file is licensed
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
package org.fabric3.groovy.introspection;

import java.io.IOException;
import java.net.URL;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyCodeSource;
import org.osoa.sca.annotations.Reference;

import org.fabric3.groovy.scdl.GroovyImplementation;
import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.IntrospectionException;
import org.fabric3.introspection.helper.IntrospectionHelper;
import org.fabric3.introspection.java.ClassWalker;
import org.fabric3.introspection.java.HeuristicProcessor;
import org.fabric3.introspection.java.ImplementationNotFoundException;
import org.fabric3.introspection.java.ImplementationProcessor;
import org.fabric3.introspection.helper.TypeMapping;
import org.fabric3.loader.common.IntrospectionContextImpl;
import org.fabric3.pojo.scdl.PojoComponentType;

/**
 * @version $Rev$ $Date$
 */
public class GroovyImplementationProcessor implements ImplementationProcessor<GroovyImplementation> {

    private final ClassWalker<GroovyImplementation> classWalker;
    private final HeuristicProcessor<GroovyImplementation> heuristic;
    private final IntrospectionHelper helper;

    public GroovyImplementationProcessor(@Reference(name = "classWalker")ClassWalker<GroovyImplementation> classWalker,
                                         @Reference(name = "heuristic")HeuristicProcessor<GroovyImplementation> heuristic,
                                         @Reference(name = "helper")IntrospectionHelper helper) {
        this.classWalker = classWalker;
        this.heuristic = heuristic;
        this.helper = helper;
    }

    public void introspect(GroovyImplementation implementation, IntrospectionContext context) throws IntrospectionException {

        Class<?> implClass = loadImplementation(implementation, context);

        PojoComponentType componentType = new PojoComponentType(implClass.getName());
        componentType.setScope("STATELESS");
        implementation.setComponentType(componentType);

        TypeMapping typeMapping = helper.mapTypeParameters(implClass);

        context = new IntrospectionContextImpl(context, typeMapping);
        classWalker.walk(implementation, implClass, context);

        heuristic.applyHeuristics(implementation, implClass, context);
    }

    Class<?> loadImplementation(GroovyImplementation implementation, IntrospectionContext context) throws ImplementationNotFoundException {
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        try {
            // Set TCCL to the extension classloader as implementations may need access to Groovy classes. Also, Groovy
            // dependencies such as Antlr use the TCCL.
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            GroovyClassLoader gcl = new GroovyClassLoader(context.getTargetClassLoader());

            // if user supplied a class name, use it as the implementation
            String className = implementation.getClassName();
            if (className != null) {
                try {
                    return gcl.loadClass(className);
                } catch (ClassNotFoundException e) {
                    throw new ImplementationNotFoundException(className, e);
                }
            }

            // if user supplied a script name, compile it and use the resulting class as the implementation
            String scriptName = implementation.getScriptName();
            if (scriptName != null) {
                try {
                    URL scriptURL = gcl.getResource(scriptName);
                    if (scriptURL == null) {
                        throw new ImplementationNotFoundException(scriptName);
                    }
                    GroovyCodeSource codeSource = new GroovyCodeSource(scriptURL);
                    return gcl.parseClass(codeSource);
                } catch (IOException e) {
                    throw new ImplementationNotFoundException(scriptName, e);
                }
            }
            // we should not have been called without an implementation artifact
            throw new AssertionError();
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }
}
