/*
 * See the NOTICE file distributed with this work for information
 * regarding copyright ownership.  This file is licensed
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
