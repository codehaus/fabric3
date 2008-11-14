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

import org.fabric3.scdl.Implementation;
import org.fabric3.scdl.InjectingComponentType;
import org.fabric3.spi.introspection.IntrospectionContext;

/**
 * Interface to a service that walks a Java class and updates the implementation definition based on annotations found.  Errors and warnings are
 * reported in the IntrospectionContext.
 *
 * @version $Rev$ $Date$
 * @param <I> the type of implementation that the clas is for
 */
public interface ClassWalker<I extends Implementation<? extends InjectingComponentType>> {
    /**
     * Walk a class and update the implementation definition. If errors or warnings are encountered, they will be collated in the
     * IntrospectionContext.
     *
     * @param implementation the implementation definition
     * @param clazz          the Java class to walk
     * @param context        the current introspection context
     */
    void walk(I implementation, Class<?> clazz, IntrospectionContext context);
}
