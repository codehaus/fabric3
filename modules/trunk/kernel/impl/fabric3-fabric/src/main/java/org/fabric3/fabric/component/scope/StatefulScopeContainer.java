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
 * Scope container that manages instances in association with a backing store that is able to persist them across invocations.
 *
 * @version $Rev$ $Date$
 * @param <KEY> the type of identifier used to identify instances of this scope
 */
public abstract class StatefulScopeContainer<KEY> extends AbstractScopeContainer<KEY> {
    private final InstanceWrapperStore<KEY> store;

    public StatefulScopeContainer(Scope<KEY> scope, ScopeContainerMonitor monitor, InstanceWrapperStore<KEY> store) {
        super(scope, monitor);
        this.store = store;
    }

    public <T> void returnWrapper(AtomicComponent<T> component, WorkContext workContext, InstanceWrapper<T> wrapper)
            throws TargetDestructionException {
    }

    public void startContext(WorkContext workContext, KEY contextId, URI groupId) throws GroupInitializationException {
        store.startContext(contextId);
        super.startContext(workContext, contextId, groupId);
    }

    protected void stopContext(KEY contextId) {
        super.stopContext(contextId);
        store.stopContext(contextId);
    }

    /**
     * Return an instance wrapper containing a component implementation instance associated with the correlation key, optionally creating one if not
     * found.
     *
     * @param component   the component the implementation instance belongs to
     * @param workContext the current WorkContext
     * @param contextId   the correlation key for the component implementation instance
     * @param create      true if an instance should be created
     * @return an instance wrapper or null if not found an create is set to false
     * @throws TargetResolutionException if an error occurs returning the wrapper
     */
    protected <T> InstanceWrapper<T> getWrapper(AtomicComponent<T> component, WorkContext workContext, KEY contextId, boolean create)
            throws TargetResolutionException {
        assert contextId != null;
        InstanceWrapper<T> wrapper = store.getWrapper(component, contextId);
        if (wrapper == null && create) {
            wrapper = createInstance(component, workContext);
            wrapper.start();
            store.putWrapper(component, contextId, wrapper);
        }
        return wrapper;
    }

}
