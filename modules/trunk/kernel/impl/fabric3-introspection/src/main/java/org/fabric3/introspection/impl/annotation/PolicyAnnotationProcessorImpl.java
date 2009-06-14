/*
 * Fabric3
 * Copyright (C) 2009 Metaform Systems
 *
 * Fabric3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version, with the
 * following exception:
 *
 * Linking this software statically or dynamically with other
 * modules is making a combined work based on this software.
 * Thus, the terms and conditions of the GNU General Public
 * License cover the whole combination.
 *
 * As a special exception, the copyright holders of this software
 * give you permission to link this software with independent
 * modules to produce an executable, regardless of the license
 * terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided
 * that you also meet, for each linked independent module, the
 * terms and conditions of the license of that module. An
 * independent module is a module which is not derived from or
 * based on this software. If you modify this software, you may
 * extend this exception to your version of the software, but
 * you are not obligated to do so. If you do not wish to do so,
 * delete this exception statement from your version.
 *
 * Fabric3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the
 * GNU General Public License along with Fabric3.
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
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