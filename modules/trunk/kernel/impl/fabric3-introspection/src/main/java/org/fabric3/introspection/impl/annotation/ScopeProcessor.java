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
package org.fabric3.introspection.impl.annotation;

import org.osoa.sca.annotations.Scope;

import org.fabric3.introspection.AbstractAnnotationProcessor;
import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.IntrospectionException;
import org.fabric3.scdl.Implementation;
import org.fabric3.scdl.InjectingComponentType;

/**
 * @version $Rev$ $Date$
 */
public class ScopeProcessor<I extends Implementation<? extends InjectingComponentType>> extends AbstractAnnotationProcessor<Scope, I> {

    public ScopeProcessor() {
        super(Scope.class);
    }

    public void visitType(Scope annotation, Class<?> type, I implementation, IntrospectionContext context) throws IntrospectionException {
        String scopeName = annotation.value();
        implementation.getComponentType().setScope(scopeName);
    }
}
