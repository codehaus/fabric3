package org.fabric3.tests.binding.harness.callback;

import junit.framework.TestCase;
import org.osoa.sca.annotations.Reference;

/**
 * @version $Revision$ $Date$
 */
public class CallbackTest extends TestCase {
    @Reference
    protected SyncClientService syncClient;

    public void testSyncCallback() throws Exception {
        syncClient.invoke("test");
    }
}
