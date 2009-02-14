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
package org.fabric3.tutorials.clustercalc.calculator;

import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Scope;
import org.oasisopen.sca.annotation.Callback;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Destroy;

/**
 * @version $Revision$ $Date$
 */
@EagerInit
@Scope("COMPOSITE")
public class CalculatorServiceImpl implements CalculatorService {
    @Callback
    protected CalculatorServiceCallback callback;

    @Init
    public void init() {
        System.out.println("Deployed calculator");
    }

    @Destroy
    public void destroy() {
        System.out.println("Undeploying calculator");
    }

    public void add(String id, double n1, double n2) {
        System.out.println("adding");
        callback.onResult(id, n1 + n2);
    }

    public void subtract(String id, double n1, double n2) {
        System.out.println("subtracting");
        callback.onResult(id, n1 - n2);
    }

    public void multiply(String id, double n1, double n2) {
        System.out.println("multiplying");
        callback.onResult(id, n1 * n2);
    }

    public void divide(String id, double n1, double n2) {
        System.out.println("dividing");
        callback.onResult(id, n1 / n2);
    }
}
