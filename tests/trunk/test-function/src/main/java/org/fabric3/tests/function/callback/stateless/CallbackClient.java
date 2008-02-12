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

import java.util.concurrent.CountDownLatch;

import org.osoa.sca.ComponentContext;
import org.osoa.sca.annotations.Context;
import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Service;

/**
 * @version $Rev$ $Date$
 */
@Service(ClientService.class)
public class CallbackClient implements ClientService, CallbackService {
    private boolean callback;

    @Reference
    protected ForwardService forwardService;

//    @Reference
//    protected ServiceReference<ForwardService> serviceReference;

    @Context
    ComponentContext context;

    public boolean isCallback() {
        return callback;
    }

    public void resetCallback() {
        callback = false;
    }

    public void invoke(CountDownLatch latch) {
        forwardService.invoke(latch);
    }

    public String invokeSync() {
        return forwardService.invokeSync();
    }

    public void invokeServiceReferenceCallback(CountDownLatch latch) {
        forwardService.invokeServiceReferenceCallback(latch);
    }

    public void invokeMultipleHops(CountDownLatch latch) {
        forwardService.invokeMultipleHops(latch);
    }

    public void onCallback(CountDownLatch latch) {
        callback = true;
        latch.countDown();
    }

    public void onServiceReferenceCallback(CountDownLatch latch) {
//        if (serviceReference.getCallbackID() != null) {
//            throw new AssertionError("Callback ID not set");
//        }
//        callback = true;
//        latch.countDown();
    }

    public void onSyncCallback() {
        callback = true;
    }

}
