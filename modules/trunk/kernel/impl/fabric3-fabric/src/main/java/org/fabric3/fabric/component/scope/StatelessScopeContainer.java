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

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Service;

import org.fabric3.api.annotation.Monitor;
import org.fabric3.scdl.Scope;
import org.fabric3.spi.ObjectCreationException;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.component.AtomicComponent;
import org.fabric3.spi.component.ExpirationPolicy;
import org.fabric3.spi.component.GroupInitializationException;
import org.fabric3.spi.component.InstanceWrapper;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.component.TargetDestructionException;
import org.fabric3.spi.component.TargetResolutionException;
import org.fabric3.spi.invocation.WorkContext;

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

    public <T> InstanceWrapper<T> getWrapper(AtomicComponent<T> component, WorkContext workContext) throws TargetResolutionException {
        try {
            InstanceWrapper<T> wrapper = component.createInstanceWrapper(workContext);
            wrapper.start();
            return wrapper;
        } catch (ObjectCreationException e) {
            throw new TargetResolutionException(e.getMessage(), component.getUri().toString(), e);
        }
    }

    public <T> void returnWrapper(AtomicComponent<T> component, WorkContext workContext, InstanceWrapper<T> wrapper)
            throws TargetDestructionException {
        wrapper.stop();
    }

    public void startContext(WorkContext workContext) throws GroupInitializationException {
        // do nothing
    }

    public void startContext(WorkContext workContext, ExpirationPolicy policy) throws GroupInitializationException {
        // do nothing
    }

    public void joinContext(WorkContext workContext) throws GroupInitializationException {
        // do nothing
    }

    public void joinContext(WorkContext workContext, ExpirationPolicy policy) throws GroupInitializationException {
        // do nothing
    }

    public void stopContext(WorkContext workContext) {
    }

    public void addObjectFactory(AtomicComponent<?> component, ObjectFactory<?> factory, String referenceName, Object key) {
    }

    public void reinject() {
    }

}
