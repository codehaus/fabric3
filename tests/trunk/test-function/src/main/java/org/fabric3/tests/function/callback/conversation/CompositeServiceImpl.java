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
package org.fabric3.tests.function.callback.conversation;

import org.osoa.sca.annotations.Callback;
import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Scope;
import org.osoa.sca.annotations.Service;

/**
 * @version $Revision$ $Date$
 */
@Scope("COMPOSITE")
@Service(interfaces = {CompositeService.class, CallbackService.class})
public class CompositeServiceImpl implements CompositeService, CallbackService {
    @Callback
    protected CallbackService callbackService;

    @Reference
    protected ForwardService forwardService;

    public void invoke() {
        forwardService.invoke();
    }

    public void invoke2() {
        forwardService.invoke2();
    }

    public void onCallback() {
        // route to conversational callback
        callbackService.onCallback();
    }

    public void end() {
        // route to conversational callback
        callbackService.end();
    }
}
