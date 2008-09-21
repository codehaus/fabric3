package org.fabric3.api;

import org.osoa.sca.RequestContext;

/**
 * A Fabric3 extension to the SCA RequestContext API. Components may reference this interface when for fields or setters marked with @Context instead
 * of the SCA RequestContext variant. For example:
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
public interface F3RequestContext extends RequestContext {

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