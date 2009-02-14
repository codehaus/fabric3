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
import org.fabric3.tutorials.clustercalc.calculator.CalculatorServiceCallback;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Reference;
import org.oasisopen.sca.annotation.Scope;
import org.oasisopen.sca.annotation.Service;

import java.util.UUID;

/**
 * @version $Revision$ $Date$
 */
@EagerInit
@Scope("COMPOSITE")
@Service(CalculatorServiceCallback.class)
public class ClientServiceImpl implements ClientService, CalculatorServiceCallback {
    private CalculatorService calculator;

    public ClientServiceImpl(@Reference(name = "calculator") CalculatorService calculator) {
        this.calculator = calculator;
    }

    @Init
    public void init() {
        System.out.println("Deployed calculator client");
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            String id = UUID.randomUUID().toString();
            calculator.add(id, 1, 1);
            id = UUID.randomUUID().toString();
            calculator.add(id, 1, 1);
            calculator.add(id, 1, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onResult(String id, double result) {
        System.out.println("Result: " + result);
    }

}
