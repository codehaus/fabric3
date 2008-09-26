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
package org.fabric3.pojo.component;

import junit.framework.TestCase;

/**
 * @version $Revision$ $Date$
 */
public class RenewableExpirationPolicyTestCase extends TestCase {

    public void testExpiration() throws Exception {
        RenewableExpirationPolicy policy = new RenewableExpirationPolicy(System.currentTimeMillis(), 10000);
        Thread.sleep(1);
        assertTrue(policy.isExpired());
    }

    public void testNonExpiration() throws Exception {
        RenewableExpirationPolicy policy = new RenewableExpirationPolicy(System.currentTimeMillis(), 10000);
        policy.renew();
        Thread.sleep(1);
        assertFalse(policy.isExpired());
    }

}
