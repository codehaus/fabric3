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
    
	public static final QName IMPLEMENTATION_SYSTEM = new QName("urn:fabric3.org:implementation", "implementation.system");

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
