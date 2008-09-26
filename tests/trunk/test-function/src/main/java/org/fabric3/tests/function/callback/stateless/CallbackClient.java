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

import org.osoa.sca.ComponentContext;
import org.osoa.sca.annotations.Context;
import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Service;

import org.fabric3.tests.function.callback.common.CallbackData;

/**
 * @version $Rev$ $Date$
 */
@Service(interfaces = {ForwardService.class, ClientService.class, CallbackService.class})
public class CallbackClient implements ForwardService, CallbackService, ClientService {

    @Reference
    protected ForwardService forwardService;

    @Property
    protected boolean fail;

//    @Reference
//    protected ServiceReference<ForwardService> serviceReference;

    @Context
    ComponentContext context;

    public void invoke(CallbackData data) {
        forwardService.invoke(data);
    }

    public String invokeSync(CallbackData data) {
        return forwardService.invokeSync(data);
    }

    public void invokeServiceReferenceCallback(CallbackData data) {
        forwardService.invokeServiceReferenceCallback(data);
    }

    public void invokeMultipleHops(CallbackData data) {
        forwardService.invokeMultipleHops(data);
    }

    public void onCallback(CallbackData data) {
        data.callback();
        data.getLatch().countDown();
    }

    public void onServiceReferenceCallback(CallbackData data) {
//        data.callback();
//        latch.countDown();
    }

    public void onSyncCallback(CallbackData data) {
        data.callback();
    }

}
