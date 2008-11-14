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
package org.fabric3.web.introspection;

import org.fabric3.spi.introspection.IntrospectionContext;

/**
 * Introspects the contents of a web application for a web component and generates a corresponding component type. Introspected information will be
 * derive from implementation classes and the web.xml deployment descriptor.
 *
 * @version $Revision$ $Date$
 */
public interface WebImplementationIntrospector {
    /**
     * Introspect the web application.
     *
     * @param implementation the web component implementation
     * @param context        the current introspection context
     */
    void introspect(WebImplementation implementation, IntrospectionContext context);

}
