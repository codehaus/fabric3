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
 * Interface for processing an implementation to introspect its component type.
 *
 * @version $Rev$ $Date$
 * @param <I>            the type of Implementation an implementation can handle
 */
public interface ImplementationProcessor<I extends Implementation<? extends AbstractComponentType<?, ?, ?, ?>>> {

    /**
     * Introspects an implementation and derives the associated component type. If errors or warnings are encountered, they will be collated in the
     * IntrospectionContext.
     *
     * @param implementation the implmentation to inspect
     * @param context        the introspection context
     */
    void introspect(I implementation, IntrospectionContext context);
}
