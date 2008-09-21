package org.fabric3.tests.function.headers;

import org.osoa.sca.annotations.Context;

import org.fabric3.api.F3RequestContext;

/**
 * @version $Revision$ $Date$
 */
public class HeaderServiceImpl implements HeaderService {

    @Context
    protected F3RequestContext context;

    public void invokeTestHeader() {
        String header = context.getHeader(String.class, "header");
        if (!"test".equals(header)) {
            throw new AssertionError("Header not propagated properly. Expected value was 'test', received '" + header + "'");
        }
    }

    public void invokeTestHeaderCleared() {
        String header = context.getHeader(String.class, "header");
        if (header != null) {
            throw new AssertionError("Header not cleared from request context");
        }
    }
}
