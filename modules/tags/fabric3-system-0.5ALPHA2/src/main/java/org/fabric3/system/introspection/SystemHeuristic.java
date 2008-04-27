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

import org.osoa.sca.annotations.Reference;

import org.fabric3.introspection.java.HeuristicProcessor;
import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.IntrospectionException;
import org.fabric3.system.scdl.SystemImplementation;

/**
 * Controls the order in which system implementation heuristics are applied.
 *
 * @version $Rev$ $Date$
 */
public class SystemHeuristic implements HeuristicProcessor<SystemImplementation> {
    private final HeuristicProcessor<SystemImplementation> serviceHeuristic;
    private final HeuristicProcessor<SystemImplementation> constructorHeuristic;
    private final HeuristicProcessor<SystemImplementation> injectionHeuristic;

    public SystemHeuristic(@Reference(name="service") HeuristicProcessor<SystemImplementation> serviceHeuristic,
                           @Reference(name="constructor") HeuristicProcessor<SystemImplementation> constructorHeuristic,
                           @Reference(name="injection") HeuristicProcessor<SystemImplementation> injectionHeuristic) {
        this.serviceHeuristic = serviceHeuristic;
        this.constructorHeuristic = constructorHeuristic;
        this.injectionHeuristic = injectionHeuristic;
    }

    public void applyHeuristics(SystemImplementation implementation, Class<?> implClass, IntrospectionContext context) throws IntrospectionException {
        serviceHeuristic.applyHeuristics(implementation, implClass, context);
        constructorHeuristic.applyHeuristics(implementation, implClass, context);
        injectionHeuristic.applyHeuristics(implementation, implClass, context);
    }
}
