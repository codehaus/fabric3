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
package org.fabric3.tests.function.callback.stateless;

import org.osoa.sca.ServiceReference;
import org.osoa.sca.annotations.Callback;
import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Service;

import org.fabric3.tests.function.callback.common.CallbackData;

/**
 * @version $Rev$ $Date$
 */
@Service(interfaces = {ForwardService.class, CallbackService.class})
public class ForwardServiceImpl implements ForwardService, CallbackService {
    @Reference
    protected EndService endService;

    @Callback
    protected CallbackService callbackService;

    @Callback
    protected ServiceReference<CallbackService> reference;

    public void invoke(CallbackData data) {
        callbackService.onCallback(data);
    }

    public String invokeSync(CallbackData data) {
        callbackService.onSyncCallback(data);
        return "receipt";
    }

    public void invokeServiceReferenceCallback(CallbackData data) {
        reference.getService().onServiceReferenceCallback(data);
    }

    public void invokeMultipleHops(CallbackData data) {
        endService.invoke(data);
    }

    public void onCallback(CallbackData data) {
        callbackService.onCallback(data);
    }

    public void onServiceReferenceCallback(CallbackData data) {
        callbackService.onServiceReferenceCallback(data);
    }

    public void onSyncCallback(CallbackData data) {
        callbackService.onSyncCallback(data);
    }
}
