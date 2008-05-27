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

import javax.xml.namespace.QName;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Scope;

import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.java.AbstractAnnotationProcessor;
import org.fabric3.scdl.Implementation;
import org.fabric3.scdl.InjectingComponentType;

/**
 * @version $Rev$ $Date$
 */
public class EagerInitProcessor<I extends Implementation<? extends InjectingComponentType>> extends AbstractAnnotationProcessor<EagerInit, I> {
    private static final String FABRIC3_SYSTEM_NS = "http://fabric3.org/xmlns/sca/system/2.0-alpha";
    public static final QName IMPLEMENTATION_SYSTEM = new QName(FABRIC3_SYSTEM_NS, "implementation.system");

    public EagerInitProcessor() {
        super(EagerInit.class);
    }

    public void visitType(EagerInit annotation, Class<?> type, I implementation, IntrospectionContext context) {
        if (!validateScope(type, implementation, context)) {
            return;
        }
        InjectingComponentType componentType = implementation.getComponentType();
        componentType.setInitLevel(50);
    }

    private boolean validateScope(Class<?> type, I implementation, IntrospectionContext context) {
        if (IMPLEMENTATION_SYSTEM.equals(implementation.getType())) {
            // system implementations are composite scoped by default
            return true;
        }
        Scope scope = type.getAnnotation(Scope.class);
        if (scope == null || !org.fabric3.scdl.Scope.COMPOSITE.getScope().equals(scope.value())) {
            EagerInitNotSupported warning = new EagerInitNotSupported(type);
            context.addWarning(warning);
            return false;
        }
        return true;
    }

}
