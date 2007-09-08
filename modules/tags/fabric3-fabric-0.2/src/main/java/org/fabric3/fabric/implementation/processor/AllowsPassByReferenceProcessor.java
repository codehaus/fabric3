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

import org.osoa.sca.annotations.AllowsPassByReference;

import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.pojo.processor.ImplementationProcessorExtension;
import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.pojo.processor.ProcessingException;

/**
 * Processes {@link AllowsPassByReference} on an implementation
 *
 * @version $Rev$ $Date$
 */
public class AllowsPassByReferenceProcessor extends ImplementationProcessorExtension {

    public <T> void visitClass(Class<T> clazz,
                               PojoComponentType type,
                               LoaderContext context)
        throws ProcessingException {
        AllowsPassByReference annotation = clazz.getAnnotation(AllowsPassByReference.class);
        if (annotation == null) {
            return;
        } else {
            // TODO implement
        }

    }
}
