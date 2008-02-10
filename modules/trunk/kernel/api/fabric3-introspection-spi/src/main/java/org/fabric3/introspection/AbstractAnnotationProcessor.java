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
package org.fabric3.introspection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.fabric3.scdl.Implementation;
import org.fabric3.scdl.InjectingComponentType;

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

    public void visitPackage(A annotation, Package javaPackage, I implementation, IntrospectionContext context) throws IntrospectionException {
    }

    public void visitType(A annotation, Class<?> type, I implementation, IntrospectionContext context) throws IntrospectionException {
    }

    public void visitField(A annotation, Field field, I implementation, IntrospectionContext context) throws IntrospectionException {
    }

    public void visitMethod(A annotation, Method method, I implementation, IntrospectionContext context) throws IntrospectionException {
    }

    public void visitMethodParameter(A annotation, Method method, int index, I implementation, IntrospectionContext context)
            throws IntrospectionException {
    }

    public void visitConstructor(A annotation, Constructor<?> constructor, I implementation, IntrospectionContext context)
            throws IntrospectionException {
    }

    public void visitConstructorParameter(A annotation, Constructor<?> constructor, int index, I implementation, IntrospectionContext context)
            throws IntrospectionException {
    }
}
