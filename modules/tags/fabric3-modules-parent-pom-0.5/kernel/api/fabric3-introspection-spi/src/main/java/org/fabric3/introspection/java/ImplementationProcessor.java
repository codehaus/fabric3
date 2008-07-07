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

import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.scdl.AbstractComponentType;
import org.fabric3.scdl.Implementation;

/**
 * Interface for processing an implementation to introspect its component type.
 *
 * @version $Rev$ $Date$
 * @param <I>            the type of Implementation an implementation can handle
 */
public interface ImplementationProcessor<I extends Implementation<? extends AbstractComponentType<?, ?, ?, ?>>> {

    /**
     * Introspects an implementation and derives the associated component type. If errors or warnings are encountered, they will be collated in the
     * IntrospectionContext.
     *
     * @param implementation the implmentation to inspect
     * @param context        the introspection context
     */
    void introspect(I implementation, IntrospectionContext context);
}
