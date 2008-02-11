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
package org.fabric3.introspection.impl;

import java.util.Map;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;

import org.fabric3.scdl.Implementation;
import org.fabric3.scdl.InjectingComponentType;
import org.fabric3.introspection.IntrospectionException;
import org.fabric3.introspection.AnnotationProcessor;
import org.fabric3.introspection.IntrospectionContext;

/**
 * @version $Rev$ $Date$
 */
public class DefaultClassWalker<I extends Implementation<? extends InjectingComponentType>> {

    private final Map<Class<? extends Annotation>, AnnotationProcessor<? extends Annotation, I>> processors;

    public DefaultClassWalker(Map<Class<? extends Annotation>, AnnotationProcessor<? extends Annotation, I>> processors) {
        this.processors = processors;
    }

    public void walk(I implementation, Class<?> clazz, IntrospectionContext context) throws IntrospectionException {
        if (!clazz.isInterface()) {
            Class<?> superClass = clazz.getSuperclass();
            if (superClass != null) {
                walk(implementation, superClass, context);
            }
        }

        for (Class<?> interfaze : clazz.getInterfaces()) {
            walk(implementation, interfaze, context);
        }

        for (Annotation annotation : clazz.getDeclaredAnnotations()) {
            visitType(annotation, clazz, implementation, context);
        }

        for (Field field : clazz.getDeclaredFields()) {
            for (Annotation annotation : field.getDeclaredAnnotations()) {
                visitField(annotation, field, implementation, context);
            }
        }

        for (Method method : clazz.getDeclaredMethods()) {
            for (Annotation annotation : method.getDeclaredAnnotations()) {
                visitMethod(annotation, method, implementation, context);
            }

            Annotation[][] parameterAnnotations = method.getParameterAnnotations();
            for (int i = 0; i < parameterAnnotations.length; i++) {
                Annotation[] annotations = parameterAnnotations[i];
                for (Annotation annotation : annotations) {
                    visitMethodParameter(annotation, method, i, implementation, context);
                }
            }
        }

        for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
            for (Annotation annotation : constructor.getDeclaredAnnotations()) {
                visitConstructor(annotation, constructor, implementation, context);
            }

            Annotation[][] parameterAnnotations = constructor.getParameterAnnotations();
            for (int i = 0; i < parameterAnnotations.length; i++) {
                Annotation[] annotations = parameterAnnotations[i];
                for (Annotation annotation : annotations) {
                    visitConstructorParameter(annotation, constructor, i, implementation, context);
                }
            }
        }
    }

    private <A extends Annotation> void visitType(A annotation, Class<?> clazz, I implementation, IntrospectionContext context) throws IntrospectionException {
        AnnotationProcessor<A, I> processor = getProcessor(annotation);
        if (processor != null) {
            processor.visitType(annotation, clazz, implementation, context);
        }
    }

    private <A extends Annotation> void visitField(A annotation, Field field, I implementation, IntrospectionContext context) throws IntrospectionException {
        AnnotationProcessor<A, I> processor = getProcessor(annotation);
        if (processor != null) {
            processor.visitField(annotation, field, implementation, context);
        }
    }

    private <A extends Annotation> void visitMethod(A annotation, Method method, I implementation, IntrospectionContext context) throws IntrospectionException {
        AnnotationProcessor<A, I> processor = getProcessor(annotation);
        if (processor != null) {
            processor.visitMethod(annotation, method, implementation, context);
        }
    }

    private <A extends Annotation> void visitMethodParameter(A annotation, Method method, int index, I implementation, IntrospectionContext context) throws IntrospectionException {
        AnnotationProcessor<A, I> processor = getProcessor(annotation);
        if (processor != null) {
            processor.visitMethodParameter(annotation, method, index, implementation, context);
        }
    }

    private <A extends Annotation> void visitConstructor(A annotation, Constructor<?> constructor, I implementation, IntrospectionContext context) throws IntrospectionException {
        AnnotationProcessor<A, I> processor = getProcessor(annotation);
        if (processor != null) {
            processor.visitConstructor(annotation, constructor, implementation, context);
        }
    }

    private <A extends Annotation> void visitConstructorParameter(A annotation, Constructor<?> constructor, int index, I implementation, IntrospectionContext context) throws IntrospectionException {
        AnnotationProcessor<A, I> processor = getProcessor(annotation);
        if (processor != null) {
            processor.visitConstructorParameter(annotation, constructor, index, implementation, context);
        }
    }

    @SuppressWarnings("unchecked")
    private <A extends Annotation> AnnotationProcessor<A, I> getProcessor(A annotation) {
        return (AnnotationProcessor<A, I>) processors.get(annotation.getClass());
    }
}
