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
package org.fabric3.groovy.introspection;

import org.fabric3.groovy.scdl.GroovyImplementation;
import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.java.HeuristicProcessor;
import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.scdl.Signature;

/**
 * @version $Rev$ $Date$
 */
public class GroovyHeuristic implements HeuristicProcessor<GroovyImplementation> {

    public void applyHeuristics(GroovyImplementation implementation, Class<?> implClass, IntrospectionContext context) {

        PojoComponentType componentType = implementation.getComponentType();

        if (componentType.getConstructor() == null) {
            try {
                componentType.setConstructor(new Signature(implClass.getConstructor()));
            } catch (NoSuchMethodException e) {
                throw new AssertionError();
            }
        }
    }
}
