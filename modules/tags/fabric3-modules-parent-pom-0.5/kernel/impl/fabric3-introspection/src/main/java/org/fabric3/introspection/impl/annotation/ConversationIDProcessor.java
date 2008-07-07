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

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.osoa.sca.annotations.ConversationID;

import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.java.AbstractAnnotationProcessor;
import org.fabric3.scdl.FieldInjectionSite;
import org.fabric3.scdl.Implementation;
import org.fabric3.scdl.InjectableAttribute;
import org.fabric3.scdl.InjectingComponentType;
import org.fabric3.scdl.InjectionSite;
import org.fabric3.scdl.MethodInjectionSite;

/**
 * @version $Rev$ $Date$
 */
public class ConversationIDProcessor<I extends Implementation<? extends InjectingComponentType>> extends AbstractAnnotationProcessor<ConversationID, I> {

    public ConversationIDProcessor() {
        super(ConversationID.class);
    }

    public void visitField(ConversationID annotation, Field field, I implementation, IntrospectionContext context) {
        InjectionSite site = new FieldInjectionSite(field);
        implementation.getComponentType().addInjectionSite(InjectableAttribute.CONVERSATION_ID, site);
    }

    public void visitMethod(ConversationID annotation, Method method, I implementation, IntrospectionContext context) {
        InjectionSite site = new MethodInjectionSite(method, 0);
        implementation.getComponentType().addInjectionSite(InjectableAttribute.CONVERSATION_ID, site);
    }
}
