package org.fabric3.tests.function.headers;

import org.osoa.sca.annotations.Context;

import org.fabric3.api.F3RequestContext;

/**
 * @version $Revision$ $Date$
 */
public class AsyncHeaderServiceImpl implements AsyncHeaderService {

    @Context
    protected F3RequestContext context;

    public void invokeTestHeader(HeaderFuture future) {
        String header = context.getHeader(String.class, "header");
        if (!"test".equals(header)) {
            AssertionError error =
                    new AssertionError("Header not propagated properly for one-way operations. Expected value was 'test', received '" + header + "'");
            future.completeWithError(error);
        } else {
            future.complete();
        }
    }

    public void invokeTestHeaderCleared(HeaderFuture future) {
        String header = context.getHeader(String.class, "header");
        if (header != null) {
            AssertionError error = new AssertionError("Header not cleared from request context");
            future.completeWithError(error);
        } else {
            future.complete();
        }
    }

}