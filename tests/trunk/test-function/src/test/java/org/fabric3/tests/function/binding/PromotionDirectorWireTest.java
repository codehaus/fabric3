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
package org.fabric3.tests.function.binding;

import junit.framework.TestCase;
import org.osoa.sca.annotations.Reference;

import org.fabric3.tests.function.common.HelloService;

/**
 * Verifies a local wire to a service on a component will use the binding as configured on the component and not the
 * binding specified when the service is promoted at the composite level.
 *
 * @version $Rev$ $Date$
 */
public class PromotionDirectorWireTest extends TestCase {
    private HelloService helloService;

    @Reference
    public void setHelloService(HelloService helloService) {
        this.helloService = helloService;
    }

    public void testReferenceIsBound() {
        assertEquals("hello", helloService.send("hello"));
    }
}
