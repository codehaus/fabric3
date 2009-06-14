/*
 * Fabric3
 * Copyright (c) 2009 Metaform Systems
 *
 * Fabric3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version, with the
 * following exception:
 *
 * Linking this software statically or dynamically with other
 * modules is making a combined work based on this software.
 * Thus, the terms and conditions of the GNU General Public
 * License cover the whole combination.
 *
 * As a special exception, the copyright holders of this software
 * give you permission to link this software with independent
 * modules to produce an executable, regardless of the license
 * terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided
 * that you also meet, for each linked independent module, the
 * terms and conditions of the license of that module. An
 * independent module is a module which is not derived from or
 * based on this software. If you modify this software, you may
 * extend this exception to your version of the software, but
 * you are not obligated to do so. If you do not wish to do so,
 * delete this exception statement from your version.
 *
 * Fabric3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the
 * GNU General Public License along with Fabric3.
 * If not, see <http://www.gnu.org/licenses/>.
*/
package org.fabric3.api;

import org.oasisopen.sca.RequestContext;

/**
 * A Fabric3 extension to the OASIS SCA RequestContext API. Components may reference this interface when for fields or setters marked with @Context
 * instead of the SCA RequestContext variant. For example:
 * <pre>
 * public class SomeComponnent implements SomeService {
 *      &#064;Context
 *      protected F3RequestContext context;
 *      //...
 * }
 * </pre>
 * At runtime, the <code>context</code> field will be injected with an instance of F3RequestContext.
 *
 * @version $Revision$ $Date$
 */
public interface Fabric3RequestContext extends RequestContext {

    /**
     * Returns the header value corresponding to a name for the current request message.
     *
     * @param type the value type
     * @param name the header name
     * @return the header value or null if not found
     */
    <T> T getHeader(Class<T> type, String name);

    /**
     * Sets a header value for the current request context. Headers will be propagated across threads for non-blocking invocations made by a component
     * when processing a request. However, headers propagation across process boundaries is binding-specific. Some bindings may propagate headers
     * while others may ignore them.
     * <p/>
     * Note that header values should be immutable since, unlike purely synchronous programming models, SCA's asynchronous model may result in
     * multiple threads simultaneously accessing a header. For example, two non-blocking invocations to local services may access the same header.
     *
     * @param name  the header name
     * @param value the header value
     */
    void setHeader(String name, Object value);

    /**
     * Clears a header for the current request context.
     *
     * @param name the header name
     */
    void removeHeader(String name);

}