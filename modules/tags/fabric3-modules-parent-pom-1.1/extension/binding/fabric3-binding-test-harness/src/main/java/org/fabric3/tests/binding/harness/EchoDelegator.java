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
package org.fabric3.tests.binding.harness;

import org.osoa.sca.annotations.Reference;

/**
 * @version $Rev$ $Date$
 */
public class EchoDelegator implements EchoService {
    private final EchoService delegate;

    public EchoDelegator(@Reference EchoService delegate) {
        this.delegate = delegate;
    }

    public String echoString(String message) {
        return delegate.echoString(message);
    }

    public int echoInt(int value) {
        return delegate.echoInt(value);
    }

    public String echoFault() throws EchoFault {
        return delegate.echoFault();
    }

    public String echoMultiParam(String param1, double param2, double param3) {
        return delegate.echoMultiParam(param1, param2, param3);
    }
}
