/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 * --- Original Apache License ---
 *
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
package org.fabric3.spi.introspection.java;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.fabric3.model.type.component.Implementation;
import org.fabric3.model.type.java.InjectingComponentType;
import org.fabric3.spi.introspection.IntrospectionContext;

/**
 * Abstract base class for annotation processors that provides default implementations of the interface methods that simply return.
 *
 * @version $Rev$ $Date$
 */
public abstract class AbstractAnnotationProcessor<A extends Annotation, I extends Implementation<? extends InjectingComponentType>> implements AnnotationProcessor<A, I> {
    private final Class<A> type;

    /**
     * Constructor binding the annotation type.
     *
     * @param type the annotation type
     */
    protected AbstractAnnotationProcessor(Class<A> type) {
        this.type = type;
    }

    public Class<A> getType() {
        return type;
    }

    public void visitPackage(A annotation, Package javaPackage, I implementation, IntrospectionContext context) {
    }

    public void visitType(A annotation, Class<?> type, I implementation, IntrospectionContext context) {
    }

    public void visitField(A annotation, Field field, I implementation, IntrospectionContext context) {
    }

    public void visitMethod(A annotation, Method method, I implementation, IntrospectionContext context) {
    }

    public void visitMethodParameter(A annotation, Method method, int index, I implementation, IntrospectionContext context) {
    }

    public void visitConstructor(A annotation, Constructor<?> constructor, I implementation, IntrospectionContext context) {
    }

    public void visitConstructorParameter(A annotation, Constructor<?> constructor, int index, I implementation, IntrospectionContext context) {
    }
}
