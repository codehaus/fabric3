/*
 * Fabric3
 * Copyright � 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the �License�), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an �as is� basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.binding.jms.test.async;

import java.util.concurrent.CountDownLatch;

import junit.framework.TestCase;
import org.osoa.sca.annotations.Reference;

/**
 * @version $Revision$ $Date$
 */
public class AsyncServiceTest extends TestCase {
    private CountDownLatch latch = new CountDownLatch(1);
    @Reference
    protected AsyncService service;

    public void testInvoke() throws Exception {
        service.invoke("test");
        latch.await();
    }

    public void onResult(String result) {
        assertEquals("test", result);
        latch.countDown();
    }
}
