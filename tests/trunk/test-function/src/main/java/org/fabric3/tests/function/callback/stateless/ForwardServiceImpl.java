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

import org.osoa.sca.ServiceReference;
import org.osoa.sca.annotations.Callback;
import org.osoa.sca.annotations.Reference;

/**
 * @version $Rev$ $Date$
 */
public class ForwardServiceImpl implements ForwardService, CallbackService {
    @Reference
    protected EndService endService;

    @Callback
    protected CallbackService callbackService;

    @Callback
    protected ServiceReference<CallbackService> reference;

    public void invoke(CountDownLatch latch) {
        callbackService.onCallback(latch);
    }

    public String invokeSync() {
        callbackService.onSyncCallback();
        return "receipt";
    }

    public void invokeServiceReferenceCallback(CountDownLatch latch) {
        reference.getService().onServiceReferenceCallback(latch);
    }

    public void invokeMultipleHops(CountDownLatch latch) {
        endService.invoke(latch);
    }

    public void onCallback(CountDownLatch latch) {
        callbackService.onCallback(latch);
    }

    public void onServiceReferenceCallback(CountDownLatch latch) {
        callbackService.onServiceReferenceCallback(latch);
    }

    public void onSyncCallback() {
        callbackService.onSyncCallback();
    }
}
