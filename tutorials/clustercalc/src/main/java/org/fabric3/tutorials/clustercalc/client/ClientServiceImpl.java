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
package org.fabric3.tutorials.clustercalc.client;

import org.fabric3.tutorials.clustercalc.calculator.CalculatorService;
import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Scope;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @version $Revision$ $Date$
 */
@EagerInit
@Scope("COMPOSITE")
public class ClientServiceImpl implements ClientService {
    private CalculatorService calculator;
    private ScheduledExecutorService executor;

    public ClientServiceImpl(@Reference(name = "calculator") CalculatorService calculator) {
        this.calculator = calculator;
    }

    @Init
    public void init() {
        System.out.println("Deployed calculator client");
        executor = Executors.newScheduledThreadPool(1);
        executor.scheduleWithFixedDelay(new Invoker(), 0, 2, TimeUnit.SECONDS);
    }

    @Destroy
    public void destroy() {
        executor.shutdownNow();
    }

    private class Invoker implements Runnable {

        public void run() {
            System.out.println("Executing....");
            try {
                calculator.add(1, 1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
