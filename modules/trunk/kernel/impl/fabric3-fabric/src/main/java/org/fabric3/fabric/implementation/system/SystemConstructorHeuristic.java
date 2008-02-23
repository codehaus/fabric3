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
package org.fabric3.fabric.implementation.system;

import java.lang.reflect.Constructor;

import org.fabric3.introspection.HeuristicProcessor;
import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.IntrospectionException;
import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.scdl.Signature;

/**
 * Heuristic that selects the constructor to use.
 *
 * @version $Rev$ $Date$
 */
public class SystemConstructorHeuristic implements HeuristicProcessor<SystemImplementation> {

    public void applyHeuristics(SystemImplementation implementation, Class<?> implClass, IntrospectionContext context) throws IntrospectionException {
        PojoComponentType componentType = implementation.getComponentType();

        // if there is already a defined constructor then do nothing
        if (componentType.getConstructor() != null) {
            return;
        }

        Signature signature = findConstructor(implClass);
        componentType.setConstructor(signature);
    }

    /**
     * Find the constructor to use.
     * <p/>
     * For now, we require that the class have a single constructor or one annotated with @Constructor. If there is more than one, then an
     * org.osoa.sca.annotations.Constructor annotation must be used.
     *
     * @param implClass the class we are inspecting
     * @return the signature of the constructor to use
     * @throws IntrospectionException if there is a problem with the user's class
     */
    Signature findConstructor(Class<?> implClass) throws IntrospectionException {
        Constructor<?>[] constructors = implClass.getDeclaredConstructors();
        Constructor<?> selected = null;
        if (constructors.length == 1) {
            selected = constructors[0];
        } else {
            for (Constructor<?> constructor : constructors) {
                if (constructor.isAnnotationPresent(org.osoa.sca.annotations.Constructor.class)) {
                    if (selected != null) {
                        throw new AmbiguousConstructorException(implClass.getName());
                    }
                    selected = constructor;
                }
            }
            if (selected == null) {
                throw new NoConstructorException(implClass.getName());
            }
        }
        return new Signature(selected);
    }

}