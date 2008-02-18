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

import org.fabric3.introspection.HeuristicProcessor;
import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.IntrospectionException;
import org.fabric3.pojo.scdl.PojoComponentType;

/**
 * Heuristic processor that locates unannotated Property and Reference dependencies.
 *
 * @version $Rev$ $Date$
 */
public class SystemConstructorHeuristic implements HeuristicProcessor<SystemImplementation> {

    public void applyHeuristics(SystemImplementation implementation, Class<?> implClass, IntrospectionContext context) throws IntrospectionException {
        PojoComponentType componentType = implementation.getComponentType();

        // if there is already a defined constructor then do nothing
        if (componentType.getConstructorDefinition() != null) {
            return;
        }

        
    }
}