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

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.xml.namespace.QName;

import org.oasisopen.sca.annotation.Intent;
import org.oasisopen.sca.annotation.PolicySets;
import org.oasisopen.sca.annotation.Qualifier;
import org.oasisopen.sca.annotation.Requires;

import org.fabric3.model.type.PolicyAware;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.java.PolicyAnnotationProcessor;

/**
 * @version $Rev: 6194 $ $Date: 2008-12-05 07:52:09 -0800 (Fri, 05 Dec 2008) $
 */
public class PolicyAnnotationProcessorImpl implements PolicyAnnotationProcessor {

    public void process(Annotation annotation, PolicyAware modelObject, IntrospectionContext context) {
        if (annotation instanceof Requires) {
            processRequires((Requires) annotation, modelObject, context);
        } else if (annotation instanceof PolicySets) {
            processPolicySets((PolicySets) annotation, modelObject, context);
        } else {
            processIntentAnnotation(annotation, modelObject, context);
        }
    }

    private void processRequires(Requires annotation, PolicyAware modelObject, IntrospectionContext context) {
        String[] intents = annotation.value();
        for (String intent : intents) {
            try {
                QName qName = QName.valueOf(intent);
                modelObject.addIntent(qName);
            } catch (IllegalArgumentException e) {
                InvalidIntentName error = new InvalidIntentName(intent, e);
                context.addError(error);
            }
        }
    }

    private void processPolicySets(PolicySets annotation, PolicyAware modelObject, IntrospectionContext context) {
        String[] policySets = annotation.value();
        for (String set : policySets) {
            try {
                QName qName = QName.valueOf(set);
                modelObject.addPolicySet(qName);
            } catch (IllegalArgumentException e) {
                InvalidIntentName error = new InvalidIntentName(set, e);
                context.addError(error);
            }
        }
    }

    private void processIntentAnnotation(Annotation annotation, PolicyAware modelObject, IntrospectionContext context) {
        Class<? extends Annotation> annotClass = annotation.annotationType();
        if (annotClass.isAnnotationPresent(Intent.class)) {
            Intent intent = annotClass.getAnnotation(Intent.class);
            String val = intent.value();
            try {
                String[] qualifiers = null;
                for (Method method : annotClass.getMethods()) {
                    if (method.isAnnotationPresent(Qualifier.class)) {
                        // iterate methods until one with @Qualified is found
                        Class<?> type = method.getReturnType();
                        if (type.isArray() && (String.class.equals(type.getComponentType()))) {
                            // multiple qualifiers as return type s String[]
                            qualifiers = (String[]) method.invoke(annotation);
                        } else if (String.class.equals(type)) {
                            // single qualifier as return type s String[]
                            String ret = (String) method.invoke(annotation);
                            qualifiers = new String[]{ret};
                        }
                        break;
                    }
                }
                if (qualifiers == null || qualifiers.length < 1 || qualifiers[0].length() < 1) {
                    // no qualifiers
                    QName name = QName.valueOf(val);
                    modelObject.addIntent(name);
                } else {
                    for (String qualifier : qualifiers) {
                        QName name = QName.valueOf(qualifier);
                        modelObject.addIntent(name);
                    }
                }
            } catch (IllegalArgumentException e) {
                context.addError(new InvalidIntentName(val, e));
            } catch (IllegalAccessException e) {
                context.addError(new InvalidIntentName(val, e));
            } catch (InvocationTargetException e) {
                context.addError(new InvalidIntentName(val, e));
            }
        }
    }

}