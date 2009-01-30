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

import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Scope;

/**
 * @version $Revision$ $Date$
 */
@EagerInit
@Scope("COMPOSITE")
public class CalculatorServiceImpl implements CalculatorService {

    @Init
    public void init() {
        System.out.println("Deployed calculator");
    }

    @Destroy
    public void destroy() {
        System.out.println("Undeploying calculator");
    }

    public void add(double n1, double n2) {
        System.out.println("adding");
    }

    public void subtract(double n1, double n2) {
        System.out.println("subtracting");
    }

    public void multiply(double n1, double n2) {
        System.out.println("multiplying");
    }

    public void divide(double n1, double n2) {
        System.out.println("dividing");
    }
}
