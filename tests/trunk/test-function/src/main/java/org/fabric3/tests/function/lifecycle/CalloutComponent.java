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
package org.fabric3.tests.function.lifecycle;

import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

/**
 * Used to verify invocations during @Init and @Destroy. Best practice dictates services should not be invoked during @Destroy but this serves to
 * verify the runtime functions properly if it is done.
 *
 * @version $Revision$ $Date$
 */
public class CalloutComponent implements CalloutService {
    @Reference
    protected CalloutTarget target;

    @Init
    public void init() {
        target.invoke();
    }

    @Destroy
    public void destroy() {
        target.invoke();
    }


    public void invoke() {
        // no-op, just ensure the implementation was dispatched to
    }
}
