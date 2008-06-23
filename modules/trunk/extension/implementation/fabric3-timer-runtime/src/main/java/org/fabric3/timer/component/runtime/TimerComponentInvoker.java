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
package org.fabric3.timer.component.runtime;

import org.fabric3.pojo.PojoWorkContextTunnel;
import org.fabric3.spi.component.InstanceDestructionException;
import org.fabric3.spi.component.InstanceLifecycleException;
import org.fabric3.spi.component.InstanceWrapper;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.invocation.CallFrame;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.wire.InvocationRuntimeException;

/**
 * Implementation registered with the runtime TimerService to receive notifications and invoke a component instance when a trigger has fired.
 *
 * @version $Revision$ $Date$
 */
public class TimerComponentInvoker<T> implements Runnable {
    private TimerComponent<T> component;
    private ScopeContainer<?> scopeContainer;

    public TimerComponentInvoker(TimerComponent<T> component) {
        this.component = component;
        this.scopeContainer = component.getScopeContainer();
    }

    public void run() {
        // create a new work context
        WorkContext workContext = new WorkContext();
        CallFrame frame = new CallFrame();
        workContext.addCallFrame(frame);
        InstanceWrapper<T> wrapper;
        try {
            // TODO handle conversations
            //startOrJoinContext(workContext);
            wrapper = scopeContainer.getWrapper(component, workContext);
        } catch (InstanceLifecycleException e) {
            throw new InvocationRuntimeException(e);
        }

        try {
            Object instance = wrapper.getInstance();
            assert instance instanceof Runnable;  // all timer components must implement java.lang.Runnable
            WorkContext oldWorkContext = PojoWorkContextTunnel.setThreadWorkContext(workContext);
            try {
                ((Runnable) instance).run();
            } finally {
                PojoWorkContextTunnel.setThreadWorkContext(oldWorkContext);
            }
        } finally {
            try {
                scopeContainer.returnWrapper(component, workContext, wrapper);
                // TODO handle conversations
            } catch (InstanceDestructionException e) {
                //noinspection ThrowFromFinallyBlock
                throw new InvocationRuntimeException(e);
            }
        }

    }
}
