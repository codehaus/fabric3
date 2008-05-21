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
package org.fabric3.introspection.java;

import org.fabric3.scdl.Implementation;
import org.fabric3.scdl.InjectingComponentType;
import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.IntrospectionException;

/**
 * Interface to a service that walks a Java class and updates the implementation definition based on annotations found.  Errors and warnings are
 * reported in the IntrospectionContext.
 *
 * @version $Rev$ $Date$
 * @param <I> the type of implementation that the clas is for
 */
public interface ClassWalker<I extends Implementation<? extends InjectingComponentType>> {
    /**
     * Walk a class and update the implementation definition. If errors or warnings are encountered, they will be collated in the
     * IntrospectionContext.
     *
     * @param implementation the implementation definition
     * @param clazz          the Java class to walk
     * @param context        the current introspection context
     */
    void walk(I implementation, Class<?> clazz, IntrospectionContext context);
}
