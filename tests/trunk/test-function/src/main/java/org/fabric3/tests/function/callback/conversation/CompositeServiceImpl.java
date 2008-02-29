/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
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
