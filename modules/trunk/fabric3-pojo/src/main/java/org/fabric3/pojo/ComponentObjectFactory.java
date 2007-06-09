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
package org.fabric3.pojo;

import org.fabric3.spi.ObjectCreationException;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.component.AtomicComponent;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.component.TargetResolutionException;
import org.fabric3.spi.component.WorkContext;

/**
 * @version $Rev$ $Date$
 */
public class ComponentObjectFactory<T, CONTEXT> implements ObjectFactory<T> {
    private final AtomicComponent<T> component;
    private final ScopeContainer<CONTEXT> scopeContainer;

    public ComponentObjectFactory(AtomicComponent<T> component, ScopeContainer<CONTEXT> scopeContainer) {
        this.component = component;
        this.scopeContainer = scopeContainer;
    }

    public T getInstance() throws ObjectCreationException {
        try {
            WorkContext workContext = PojoWorkContextTunnel.getThreadWorkContext();
            return scopeContainer.getWrapper(component, workContext).getInstance();
        } catch (TargetResolutionException e) {
            throw new ObjectCreationException(e);
        }
    }
}
