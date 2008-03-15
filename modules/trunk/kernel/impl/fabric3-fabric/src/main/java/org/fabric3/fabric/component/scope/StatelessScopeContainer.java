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

import java.net.URI;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Service;

import org.fabric3.api.annotation.Monitor;
import org.fabric3.scdl.Scope;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.component.AtomicComponent;
import org.fabric3.spi.component.GroupInitializationException;
import org.fabric3.spi.component.InstanceWrapper;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.component.TargetDestructionException;
import org.fabric3.spi.component.TargetResolutionException;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.component.ExpirationPolicy;

/**
 * A scope context which manages stateless atomic component instances in a non-pooled fashion.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
@Service(ScopeContainer.class)
public class StatelessScopeContainer extends AbstractScopeContainer<Object> {

    public StatelessScopeContainer(@Monitor ScopeContainerMonitor monitor) {
        super(Scope.STATELESS, monitor);
    }

    public void startContext(WorkContext workContext, URI groupId) throws GroupInitializationException {
        super.startContext(workContext, null, (URI) null);
    }

    public void startContext(WorkContext workContext, URI groupId, ExpirationPolicy policy) throws GroupInitializationException {
        // scope does not support expiration
        this.startContext(workContext, groupId);
    }

    public <T> InstanceWrapper<T> getWrapper(AtomicComponent<T> component, WorkContext workContext) throws TargetResolutionException {
        InstanceWrapper<T> ctx = createInstance(component, workContext);
        ctx.start();
        return ctx;
    }

    public <T> void returnWrapper(AtomicComponent<T> component, WorkContext workContext, InstanceWrapper<T> wrapper)
            throws TargetDestructionException {
        wrapper.stop();
    }

    public void stopContext(WorkContext workContext) {
    }
    
    public void reinject() {
    }
    
    public void addObjectFactory(AtomicComponent<?> component, ObjectFactory<?> factory, String referenceName, Object key) {
    }

}
