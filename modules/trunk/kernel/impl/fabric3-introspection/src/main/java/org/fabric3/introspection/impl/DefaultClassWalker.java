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

import org.osoa.sca.annotations.Reference;

import org.fabric3.scdl.Implementation;
import org.fabric3.scdl.InjectingComponentType;
import org.fabric3.introspection.IntrospectionException;
import org.fabric3.introspection.java.AnnotationProcessor;
import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.java.ClassWalker;

/**
 * @version $Rev$ $Date$
 */
public class DefaultClassWalker<I extends Implementation<? extends InjectingComponentType>> implements ClassWalker<I> {

    private Map<Class<? extends Annotation>, AnnotationProcessor<? extends Annotation, I>> processors;

    /**
     * Constructor used from the bootstrapper.
     * 
     * @param processors
     */
    public DefaultClassWalker(Map<Class<? extends Annotation>, AnnotationProcessor<? extends Annotation, I>> processors) {
        this.processors = processors;
    }
    
    /**
     * Constructor used from the system SCDL. 
     * 
     * TODO This needs to be working once the re-injection is working properly.
     */
    @org.osoa.sca.annotations.Constructor
    public DefaultClassWalker() {
    }
    
    @Reference
    public void setProcessors(Map<Class<? extends Annotation>, AnnotationProcessor<? extends Annotation, I>> processors) {
        this.processors = processors;
    }

    public void walk(I implementation, Class<?> clazz, IntrospectionContext context) throws IntrospectionException {
        if (!clazz.isInterface()) {
            walkSuperClasses(implementation, clazz, context);
        }

        walkInterfaces(implementation, clazz, context);

        walkClass(implementation, clazz, context);

        walkFields(implementation, clazz, context);

        walkMethods(implementation, clazz, context);

        walkConstructors(implementation, clazz, context);
    }

    private void walkSuperClasses(I implementation, Class<?> clazz, IntrospectionContext context) throws IntrospectionException {
        Class<?> superClass = clazz.getSuperclass();
        if (superClass != null) {
            walk(implementation, superClass, context);
        }
    }

    private void walkInterfaces(I implementation, Class<?> clazz, IntrospectionContext context) throws IntrospectionException {
        for (Class<?> interfaze : clazz.getInterfaces()) {
            walk(implementation, interfaze, context);
        }
    }

    private void walkClass(I implementation, Class<?> clazz, IntrospectionContext context) throws IntrospectionException {
        for (Annotation annotation : clazz.getDeclaredAnnotations()) {
            visitType(annotation, clazz, implementation, context);
        }
    }

    private void walkFields(I implementation, Class<?> clazz, IntrospectionContext context) throws IntrospectionException {
        for (Field field : clazz.getDeclaredFields()) {
            for (Annotation annotation : field.getDeclaredAnnotations()) {
                visitField(annotation, field, implementation, context);
            }
        }
    }

    private void walkMethods(I implementation, Class<?> clazz, IntrospectionContext context) throws IntrospectionException {
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
    }

    private void walkConstructors(I implementation, Class<?> clazz, IntrospectionContext context) throws IntrospectionException {
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
        return (AnnotationProcessor<A, I>) processors.get(annotation.annotationType());
    }
}
