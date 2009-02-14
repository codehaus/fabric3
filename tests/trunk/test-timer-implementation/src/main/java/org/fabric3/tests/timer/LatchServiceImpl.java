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
package org.fabric3.tests.timer;

import java.util.concurrent.CountDownLatch;

import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Scope;

/**
 * @version $Revision$ $Date$
 */
@Scope("COMPOSITE")
public class LatchServiceImpl implements LatchService {
    private CountDownLatch latch;

    public LatchServiceImpl(@Property(name = "count")int count) {
        this.latch = new CountDownLatch(count);
    }

    public void await() throws InterruptedException {
        latch.await();
    }

    public void countDown() {
        latch.countDown();
    }
}
