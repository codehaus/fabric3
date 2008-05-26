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
package org.fabric3.system.introspection;

import java.lang.reflect.Constructor;

import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.java.HeuristicProcessor;
import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.scdl.Signature;
import org.fabric3.scdl.validation.AmbiguousConstructor;
import org.fabric3.introspection.java.NoConstructorFound;
import org.fabric3.system.scdl.SystemImplementation;

/**
 * Heuristic that selects the constructor to use.
 *
 * @version $Rev$ $Date$
 */
public class SystemConstructorHeuristic implements HeuristicProcessor<SystemImplementation> {

    public void applyHeuristics(SystemImplementation implementation, Class<?> implClass, IntrospectionContext context) {
        PojoComponentType componentType = implementation.getComponentType();

        // if there is already a defined constructor then do nothing
        if (componentType.getConstructor() != null) {
            return;
        }

        Signature signature = findConstructor(implClass, context);
        componentType.setConstructor(signature);
    }

    /**
     * Find the constructor to use.
     * <p/>
     * For now, we require that the class have a single constructor or one annotated with @Constructor. If there is more than one, the default
     * constructor will be selected or an org.osoa.sca.annotations.Constructor annotation must be used.
     *
     * @param implClass the class we are inspecting
     * @param context   the introspection context to report errors and warnings
     * @return the signature of the constructor to use
     */
    Signature findConstructor(Class<?> implClass, IntrospectionContext context) {
        Constructor<?>[] constructors = implClass.getDeclaredConstructors();
        Constructor<?> selected = null;
        if (constructors.length == 1) {
            selected = constructors[0];
        } else {
            for (Constructor<?> constructor : constructors) {
                if (constructor.isAnnotationPresent(org.osoa.sca.annotations.Constructor.class)) {
                    if (selected != null) {
                        context.addError(new AmbiguousConstructor(implClass));
                        return null;
                    }
                    selected = constructor;
                }
            }
            if (selected == null) {
                try {
                    selected = implClass.getConstructor();
                } catch (NoSuchMethodException e) {
                    context.addError(new NoConstructorFound(implClass));
                    return null;
                }
            }
        }
        return new Signature(selected);
    }

}