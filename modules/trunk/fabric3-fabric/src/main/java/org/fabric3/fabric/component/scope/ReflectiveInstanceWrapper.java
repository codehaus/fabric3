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
package org.fabric3.fabric.component.scope;

import org.fabric3.spi.component.TargetInitializationException;
import org.fabric3.spi.component.TargetDestructionException;
import org.fabric3.pojo.reflection.EventInvoker;
import org.fabric3.pojo.reflection.ObjectCallbackException;

/**
 * @version $Rev$ $Date$
 */
public class ReflectiveInstanceWrapper<T> extends InstanceWrapperBase<T> {
    private final EventInvoker<T> initInvoker;
    private final EventInvoker<T> destroyInvoker;

    public ReflectiveInstanceWrapper(T instance, EventInvoker<T> initInvoker, EventInvoker<T> destroyInvoker) {
        super(instance);
        this.initInvoker = initInvoker;
        this.destroyInvoker = destroyInvoker;
    }

    public void start() throws TargetInitializationException {
        if (initInvoker != null) {
            try {
                initInvoker.invokeEvent(instance);
            } catch (ObjectCallbackException e) {
                throw new TargetInitializationException(e.getMessage(), e);
            }
        }
        super.start();
    }


    public void stop() throws TargetDestructionException {
        super.stop();
        if (destroyInvoker != null) {
            try {
                destroyInvoker.invokeEvent(instance);
            } catch (ObjectCallbackException e) {
                throw new TargetDestructionException(e.getMessage(), e);
            }
        }
    }
}
