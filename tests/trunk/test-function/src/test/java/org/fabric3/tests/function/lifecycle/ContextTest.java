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
package org.fabric3.tests.function.lifecycle;

import junit.framework.TestCase;
import org.oasisopen.sca.ComponentContext;
import org.oasisopen.sca.RequestContext;
import org.oasisopen.sca.annotation.Context;
import org.oasisopen.sca.annotation.Property;

/**
 * @version $Rev$ $Date$
 */
public class ContextTest extends TestCase {

    @Context
    protected RequestContext requestContext;

    @Context
    protected ComponentContext componentContext;

    @Property
    protected String uri;

    public void testRequestContext() {
        assertNotNull(requestContext);
        assertSame(requestContext, componentContext.getRequestContext());
    }

    public void testComponentContext() {
        assertNotNull(componentContext);
        assertEquals(uri, componentContext.getURI());
    }
}
