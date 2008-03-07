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
package org.fabric3.fabric.implementation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.osoa.sca.annotations.Reference;

import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.java.IntrospectionHelper;
import org.fabric3.introspection.java.TypeMapping;
import org.fabric3.loader.common.IntrospectionContextImpl;
import org.fabric3.pojo.processor.ImplementationProcessor;
import org.fabric3.pojo.processor.IntrospectionRegistry;
import org.fabric3.pojo.processor.JavaIntrospectionHelper;
import org.fabric3.pojo.processor.ProcessingException;
import org.fabric3.pojo.scdl.PojoComponentType;

/**
 * Default implementation of the <code>IntrospectionRegistry</code>
 *
 * @version $Rev$ $Date$
 */
public class IntrospectionRegistryImpl implements IntrospectionRegistry {

    private final IntrospectionHelper helper;
    private final Monitor monitor;
    private List<ImplementationProcessor> cache = new ArrayList<ImplementationProcessor>();

    public IntrospectionRegistryImpl(@org.fabric3.api.annotation.Monitor Monitor monitor,
                                     @Reference IntrospectionHelper helper) {
        this.monitor = monitor;
        this.helper = helper;
    }

    public void registerProcessor(ImplementationProcessor processor) {
        monitor.register(processor);
        cache.add(processor);
    }

    public void unregisterProcessor(ImplementationProcessor processor) {
        monitor.unregister(processor);
        cache.remove(processor);
    }

    public PojoComponentType introspect(Class<?> clazz,
                                        PojoComponentType type,
                                        IntrospectionContext context)
        throws ProcessingException {

        TypeMapping typeMapping = helper.mapTypeParameters(clazz);
        context = new IntrospectionContextImpl(context, typeMapping);

        for (ImplementationProcessor processor : cache) {
            processor.visitClass(clazz, type, context);
        }

        for (Constructor<?> constructor : clazz.getConstructors()) {
            for (ImplementationProcessor processor : cache) {
                processor.visitConstructor(constructor, type, context);
            }
        }

        Set<Method> methods = JavaIntrospectionHelper.getAllUniquePublicProtectedMethods(clazz);
        for (Method method : methods) {
            for (ImplementationProcessor processor : cache) {
                processor.visitMethod(method, type, context);
            }
        }

        Set<Field> fields = JavaIntrospectionHelper.getAllPublicAndProtectedFields(clazz);
        for (Field field : fields) {
            for (ImplementationProcessor processor : cache) {
                processor.visitField(field, type, context);
            }
        }

        Class superClass = clazz.getSuperclass();
        if (superClass != null) {
            visitSuperClass(superClass, type, context);
        }

        for (ImplementationProcessor processor : cache) {
            processor.visitEnd(clazz, type, context);
        }
        return type;
    }

    private void visitSuperClass(Class<?> clazz,
                                 PojoComponentType type,
                                 IntrospectionContext context)
        throws ProcessingException {
        if (!Object.class.equals(clazz)) {
            for (ImplementationProcessor processor : cache) {
                processor.visitSuperClass(clazz, type, context);
            }
            clazz = clazz.getSuperclass();
            if (clazz != null) {
                visitSuperClass(clazz, type, context);
            }
        }
    }

    public static interface Monitor {
        void register(ImplementationProcessor processor);

        void unregister(ImplementationProcessor processor);

        void processing(ImplementationProcessor processor);
    }
}
