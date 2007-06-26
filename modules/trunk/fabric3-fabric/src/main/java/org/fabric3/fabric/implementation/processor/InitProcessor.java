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
package org.fabric3.fabric.implementation.processor;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.osoa.sca.annotations.Init;

import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.pojo.processor.ImplementationProcessorExtension;
import org.fabric3.pojo.processor.PojoComponentType;
import org.fabric3.pojo.processor.ProcessingException;

/**
 * Processes the {@link @Init} annotation on a component implementation and updates the component type with the
 * decorated initializer method
 *
 * @version $Rev$ $Date$
 */
public class InitProcessor extends ImplementationProcessorExtension {

    public void visitMethod(Method method,
                            PojoComponentType type,
                            LoaderContext context)
        throws ProcessingException {
        Init annotation = method.getAnnotation(Init.class);
        if (annotation == null) {
            return;
        }
        if (method.getParameterTypes().length != 0) {
            throw new IllegalInitException("Initializer must not have argments", method.toString());
        }
        if (type.getInitMethod() != null) {
            throw new DuplicateInitException("More than one initializer found on implementaton");
        }
        if (Modifier.isProtected(method.getModifiers())) {
            method.setAccessible(true);
        }
        type.setInitMethod(method);
    }
}
