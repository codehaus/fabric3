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
package org.fabric3.spi.introspection.java;

import org.fabric3.model.type.component.AbstractComponentType;
import org.fabric3.model.type.component.Implementation;
import org.fabric3.spi.introspection.IntrospectionContext;

/**
 * Interface for processors that provide heuristic introspection of component implementations.
 *
 * @version $Rev$ $Date$
 */
public interface HeuristicProcessor<I extends Implementation<? extends AbstractComponentType>> {

    /**
     * Apply heuristics to an implementation and update the component type accordingly. If errors or warnings are encountered, they will be collated
     * in the IntrospectionContext.
     *
     * @param implementation the implementation to inspect
     * @param implClass      the implementation class
     * @param context        the current introspection context
     */
    void applyHeuristics(I implementation, Class<?> implClass, IntrospectionContext context);
}
