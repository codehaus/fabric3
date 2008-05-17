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

import org.fabric3.api.annotation.scope.Stateless;
import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.IntrospectionException;
import org.fabric3.introspection.java.AbstractAnnotationProcessor;
import org.fabric3.scdl.Implementation;
import org.fabric3.scdl.InjectingComponentType;
import org.osoa.sca.annotations.Scope;


public class StatelessProcessor<I extends Implementation<? extends InjectingComponentType>> extends AbstractAnnotationProcessor<Stateless, I> {

    public StatelessProcessor() {
        super(Stateless.class);
    }

    public void visitType(Stateless annotation, Class<?> type, I implementation, IntrospectionContext context) throws IntrospectionException {
        Scope scopeMetaAnnotation = annotation.annotationType().getAnnotation(Scope.class);
        String scopeName = scopeMetaAnnotation.value();
        implementation.getComponentType().setScope(scopeName);
    }
}