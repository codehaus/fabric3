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
package org.fabric3.tests.function.wiring;

import junit.framework.TestCase;
import org.oasisopen.sca.annotation.Reference;

/**
 * @version $Rev$ $Date$
 */
public class WiringTest extends TestCase {
    private TestService service;

    @Reference
    public void setTestService(TestService service) {
        this.service = service;
    }

    /**
     * Tests a wire that is explicitly targeted with a "target="  on a reference
     */
    public void testTargetedWire() {
        assertNotNull(service.getService());
    }

    /**
     * Tests a wire that is explicitly targeted with a "target=" on a constructor
     */
    public void testTargetedConstructorWire() {
        assertNotNull(service.getConstructorService());
    }

    /**
     * Tests a reference configured on the component without a 'target=' and promoted on the composite:
     * <pre>
     *      <component name="TestComponent">
     *          ...
     *          <reference name="promotedReference"/>
     *      </component>
     * <p/>
     *      <reference name="promotedReference" promote="TestComponent/promotedReference">...
     * <pre>
     */
    public void testPromotedReferences() {
        assertNotNull(service.getPromotedReference());
    }

    /**
     * Tests a reference configured solely via promotion:
     * <pre>
     *      <component name="TestComponent">
     *          ...
     *          <!-- no <reference name="promotedReference" -->
     *      </component>
     * <p/>
     *      <reference name="promotedReference" promote="TestComponent/promotedReference">...
     * <pre>
     */
    public void testNonConfiguredPromotedReferences() {
        assertNotNull(service.getNonConfiguredPromotedReference());
    }

    /**
     * Verifies a reference of multiplicity 0..n does not need to be configured
     */
    public void testOptionalNonSetReference() {
        assertNull(service.getOptionalNonSetReference());
    }
}
