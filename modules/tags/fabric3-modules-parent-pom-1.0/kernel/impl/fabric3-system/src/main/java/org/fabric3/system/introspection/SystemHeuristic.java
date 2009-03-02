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
 */
package org.fabric3.system.introspection;

import org.osoa.sca.annotations.Reference;

import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.java.HeuristicProcessor;
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

    public void applyHeuristics(SystemImplementation implementation, Class<?> implClass, IntrospectionContext context) {
        serviceHeuristic.applyHeuristics(implementation, implClass, context);
        constructorHeuristic.applyHeuristics(implementation, implClass, context);
        injectionHeuristic.applyHeuristics(implementation, implClass, context);
    }
}
