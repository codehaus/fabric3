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

import javax.annotation.PostConstruct;

import org.fabric3.scdl.Signature;
import org.fabric3.pojo.processor.ImplementationProcessorExtension;
import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.pojo.processor.ProcessingException;
import org.fabric3.introspection.IntrospectionContext;

/**
 * Processes the {@link @Init} annotation on a component implementation and updates the component type with the
 * decorated initializer method
 *
 * @version $Rev: 751 $ $Date: 2007-08-16 14:50:14 -0500 (Thu, 16 Aug 2007) $
 */
public class PostConstructProcessor extends ImplementationProcessorExtension {

    public void visitMethod(Method method,
                            PojoComponentType type,
                            IntrospectionContext context)
        throws ProcessingException {
        PostConstruct annotation = method.getAnnotation(PostConstruct.class);
        if (annotation == null) {
            return;
        }
        if (method.getParameterTypes().length != 0) {
            throw new IllegalInitException("PostConstructor must not have argments", method.toString());
        }
        if (type.getInitMethod() != null) {
            throw new DuplicateInitException("More than one initializer found on implementaton");
        }
        if (void.class != method.getReturnType()) {
            throw new IllegalInitException("PostConstructor return type must be void", method.toString());
        }
        if (Modifier.isStatic(method.getModifiers())) {
            throw new IllegalInitException("PostConstructor must not be static", method.toString());
        }
        /* JSR250 spec is a little unclear on construct methods that throw checked exceptions. It first says
         * construct methods must not throw checked exceptions but then turns around and says if it does
         * they are ignored and the instance is discarded. will leave this out for now */
        /*
        if (method.getExceptionTypes().length != 0) {
            throw new IllegalInitException("PostConstructor must not throw checked exceptions", method.toString());
        }
        */
        type.setInitMethod(new Signature(method));
    }
}
