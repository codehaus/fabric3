package org.fabric3.tests.binding.harness;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.osoa.sca.annotations.Scope;

/**
 * @version $Revision$ $Date$
 */
@Scope("COMPOSITE")
public class OneWayEchoServiceImpl implements OneWayEchoService {
    private CountDownLatch latch = new CountDownLatch(0);

    public void echoString(String message) {
        latch.countDown();
    }

    public void await() throws InterruptedException {
        latch.await(5000, TimeUnit.MILLISECONDS);
    }

}
