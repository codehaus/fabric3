package org.fabric3.tests.timer;

import java.util.concurrent.CountDownLatch;

import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Scope;

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
