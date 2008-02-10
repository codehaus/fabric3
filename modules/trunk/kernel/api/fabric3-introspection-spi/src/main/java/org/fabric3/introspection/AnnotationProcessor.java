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
 * Interface for processors that handle annotations attached to Java declarations.
 *
 * @version $Rev$ $Date$
 * @param <A> the type of annotation this processor handles
 */
public interface AnnotationProcessor<A extends Annotation, I extends Implementation<? extends InjectingComponentType>> {
    /**
     * Returns the type of annotation this processor handles.
     *
     * @return the type of annotation this processor handles
     */
    Class<A> getType();

    /**
     * Visit an annotation attached to a package declaration.
     *
     * @param annotation     the annotation
     * @param javaPackage    the package
     * @param implementation the implementation being introspected
     * @param context        the current introspection context
     * @throws IntrospectionException if there was a problem with the declaration
     */
    void visitPackage(A annotation, Package javaPackage, I implementation, IntrospectionContext context) throws IntrospectionException;

    /**
     * Visit an annotation attached to a class or interface declaration.
     *
     * @param annotation     the annotation
     * @param type           the class or interface
     * @param implementation the implementation being introspected
     * @param context        the current introspection context
     * @throws IntrospectionException if there was a problem with the declaration
     */
    void visitType(A annotation, Class<?> type, I implementation, IntrospectionContext context) throws IntrospectionException;

    /**
     * Visit an annotation attached to a field declaration.
     *
     * @param annotation     the annotation
     * @param field          the field
     * @param implementation the implementation being introspected
     * @param context        the current introspection context
     * @throws IntrospectionException if there was a problem with the declaration
     */
    void visitField(A annotation, Field field, I implementation, IntrospectionContext context) throws IntrospectionException;

    /**
     * Visit an annotation attached to a method declaration.
     *
     * @param annotation     the annotation
     * @param method         the method declaration
     * @param implementation the implementation being introspected
     * @param context        the current introspection context
     * @throws IntrospectionException if there was a problem with the declaration
     */
    void visitMethod(A annotation, Method method, I implementation, IntrospectionContext context) throws IntrospectionException;

    /**
     * Visit an annotation attached to a method parameter declaration.
     *
     * @param annotation     the annotation
     * @param method         the method declaration
     * @param index          the index of the method parameter
     * @param implementation the implementation being introspected
     * @param context        the current introspection context
     * @throws IntrospectionException if there was a problem with the declaration
     */
    void visitMethodParameter(A annotation, Method method, int index, I implementation, IntrospectionContext context) throws IntrospectionException;

    /**
     * Visit an annotation attached to a constructor declaration.
     *
     * @param annotation     the annotation
     * @param constructor    the constructor
     * @param implementation the implementation being introspected
     * @param context        the current introspection context
     * @throws IntrospectionException if there was a problem with the declaration
     */
    void visitConstructor(A annotation, Constructor<?> constructor, I implementation, IntrospectionContext context) throws IntrospectionException;

    /**
     * Visit an annotation attached to a constructor parameter declaration.
     *
     * @param annotation     the annotation
     * @param constructor    the constructor
     * @param index          the index of the constructor parameter
     * @param implementation the implementation being introspected
     * @param context        the current introspection context
     * @throws IntrospectionException if there was a problem with the declaration
     */
    void visitConstructorParameter(A annotation, Constructor<?> constructor, int index, I implementation, IntrospectionContext context)
            throws IntrospectionException;
}
