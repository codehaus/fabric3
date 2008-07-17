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
package org.fabric3.conversation.propagation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import javax.xml.namespace.QName;

import org.fabric3.api.annotation.PropagatesConversation;
import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.java.AbstractAnnotationProcessor;
import org.fabric3.scdl.Implementation;
import org.fabric3.scdl.InjectingComponentType;

/**
 * @version $Rev: 3105 $ $Date: 2008-03-15 09:47:31 -0700 (Sat, 15 Mar 2008) $
 */
public class ConversationPropagationProcessor<I extends Implementation<? extends InjectingComponentType>>
        extends AbstractAnnotationProcessor<PropagatesConversation, I> {
    public static final QName PROPAGATES_CONVERSATION_INTENT = new QName("http://fabric3.org/xmlns/sca/2.0-alpha", "propagatesConversation");

    public ConversationPropagationProcessor() {
        super(PropagatesConversation.class);
    }

    public void visitField(PropagatesConversation annotation, Field field, I implementation, IntrospectionContext context) {
    }

    public void visitMethod(PropagatesConversation annotation, Method method, I implementation, IntrospectionContext context) {
    }

    public void visitConstructorParameter(PropagatesConversation annotation,
                                          Constructor<?> constructor,
                                          int index,
                                          I implementation,
                                          IntrospectionContext context) {
    }

    public void visitType(PropagatesConversation annotation, Class<?> type, I implementation, IntrospectionContext context) {
    }
}