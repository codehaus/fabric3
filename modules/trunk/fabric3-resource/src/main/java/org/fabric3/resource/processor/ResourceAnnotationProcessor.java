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
package org.fabric3.resource.processor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.fabric3.pojo.processor.ImplementationProcessorExtension;
import org.fabric3.pojo.processor.ProcessingException;
import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.spi.loader.LoaderContext;

/**
 * Resources annotation processor for JSR 250 and F3 resource annotations.
 * 
 * @version $Revision$ $Date$
 */
public class ResourceAnnotationProcessor extends ImplementationProcessorExtension {

    /**
     * @see org.fabric3.pojo.processor.ImplementationProcessorExtension#visitConstructor(java.lang.reflect.Constructor,
     *      org.fabric3.pojo.scdl.PojoComponentType,
     *      org.fabric3.spi.loader.LoaderContext)
     */
    @Override
    public <T> void visitConstructor(Constructor<T> constructor, PojoComponentType type, LoaderContext context) throws ProcessingException {
        super.visitConstructor(constructor, type, context);
    }

    /**
     * @see org.fabric3.pojo.processor.ImplementationProcessorExtension#visitField(java.lang.reflect.Field,
     *      org.fabric3.pojo.scdl.PojoComponentType,
     *      org.fabric3.spi.loader.LoaderContext)
     */
    @Override
    public void visitField(Field field, PojoComponentType type, LoaderContext context) throws ProcessingException {
        super.visitField(field, type, context);
    }

    /**
     * @see org.fabric3.pojo.processor.ImplementationProcessorExtension#visitMethod(java.lang.reflect.Method,
     *      org.fabric3.pojo.scdl.PojoComponentType,
     *      org.fabric3.spi.loader.LoaderContext)
     */
    @Override
    public void visitMethod(Method method, PojoComponentType type, LoaderContext context) throws ProcessingException {
        super.visitMethod(method, type, context);
    }

}
