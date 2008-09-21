package org.fabric3.tests.function.headers;

import junit.framework.TestCase;
import org.osoa.sca.annotations.Context;
import org.osoa.sca.annotations.Reference;

import org.fabric3.api.F3RequestContext;

/**
 * @version $Revision$ $Date$
 */
public class HeaderTest extends TestCase {

    @Context
    protected F3RequestContext context;

    @Reference
    protected HeaderService headerService;

    @Reference
    protected AsyncHeaderService asyncHeaderService;

    public void testSetHeader() {
        context.setHeader("header", "test");
        headerService.invokeTestHeader();
        context.removeHeader("header");
        headerService.invokeTestHeaderCleared();
    }

    public void testAsyncSetHeader() throws Exception {
        context.setHeader("header", "test");
        HeaderFuture future = new HeaderFuture();
        asyncHeaderService.invokeTestHeader(future);
        AssertionError error = future.get();
        if (error != null) {
            throw error;
        }
        context.removeHeader("header");
        future = new HeaderFuture();
        asyncHeaderService.invokeTestHeaderCleared(future);
        error = future.get();
        if (error != null) {
            throw error;
        }
    }
}
