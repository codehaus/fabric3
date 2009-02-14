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
package org.fabric3.tests.function.headers;

import org.oasisopen.sca.annotation.Context;

import org.fabric3.api.Fabric3RequestContext;

/**
 * @version $Revision$ $Date$
 */
public class AsyncHeaderServiceImpl implements AsyncHeaderService {

    @Context
    protected Fabric3RequestContext context;

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