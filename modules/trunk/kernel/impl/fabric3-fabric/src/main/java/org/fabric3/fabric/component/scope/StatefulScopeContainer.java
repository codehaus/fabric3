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
package org.fabric3.fabric.component.scope;

import java.net.URI;

import org.fabric3.spi.component.AtomicComponent;
import org.fabric3.spi.component.GroupInitializationException;
import org.fabric3.spi.component.InstanceWrapper;
import org.fabric3.spi.component.InstanceWrapperStore;
import org.fabric3.spi.component.TargetDestructionException;
import org.fabric3.spi.component.TargetResolutionException;
import org.fabric3.spi.component.WorkContext;
import org.fabric3.scdl.Scope;

/**
 * Scope container that manages instances in association with a backing store that is able to persist them across
 * invocations.
 *
 * @version $Rev$ $Date$
 * @param <KEY> the type of identifier used to identify instances of this scope
 */
public class StatefulScopeContainer<KEY> extends AbstractScopeContainer<KEY> {
    private final InstanceWrapperStore<KEY> store;

    public StatefulScopeContainer(Scope<KEY> scope, ScopeContainerMonitor monitor, InstanceWrapperStore<KEY> store) {
        super(scope, monitor);
        this.store = store;
    }

    public void startContext(WorkContext workContext, URI groupId) throws GroupInitializationException {
        KEY contextId = workContext.peekCallFrame().getForwardCorrelationId(getScope().getIdentifierType());
        assert contextId != null;
        store.startContext(contextId);
        super.startContext(workContext, contextId, groupId);
    }

    public void stopContext(WorkContext workContext) {
        KEY contextId = workContext.peekCallFrame().getForwardCorrelationId(getScope().getIdentifierType());
        assert contextId != null;
        super.stopContext(workContext);
        store.stopContext(contextId);
    }

    public <T> InstanceWrapper<T> getWrapper(AtomicComponent<T> component, WorkContext workContext)
            throws TargetResolutionException {
        KEY contextId = workContext.peekCallFrame().getForwardCorrelationId(getScope().getIdentifierType());
        assert contextId != null;
        InstanceWrapper<T> wrapper = store.getWrapper(component, contextId);
        if (wrapper == null) {
            wrapper = createInstance(component, workContext);
            wrapper.start();
            store.putWrapper(component, contextId, wrapper);
        }
        return wrapper;
    }

    public <T> void returnWrapper(AtomicComponent<T> component, WorkContext workContext, InstanceWrapper<T> wrapper)
            throws TargetDestructionException {
    }
}
