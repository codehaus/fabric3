/*
 * See the NOTICE file distributed with this work for information
 * regarding copyright ownership.  This file is licensed
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.fabric3.test.wiring;

import junit.framework.TestCase;
import org.osoa.sca.annotations.Reference;

/**
 * @version $Rev$ $Date$
 */
public class WiringTest extends TestCase {
    private TestService service;

    @Reference
    public void setService(TestService service) {
        this.service = service;
    }

    /**
     * Tests a wire that is explicitly targeted with a "target="  on a reference
     */
    public void testTargetedWire() {
        assertNotNull(service.getTarget());
    }

    /**
     * Tests a wire that is explicitly targeted with a "target=" on a constructor
     */
    public void testTargetedConstructorWire() {
        assertNotNull(service.getConstructorTarget());
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

}
