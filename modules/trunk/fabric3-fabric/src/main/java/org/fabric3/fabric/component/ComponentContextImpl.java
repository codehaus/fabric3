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
package org.fabric3.fabric.component;

import org.osoa.sca.CallableReference;
import org.osoa.sca.ComponentContext;
import org.osoa.sca.RequestContext;
import org.osoa.sca.ServiceReference;
import org.osoa.sca.ServiceRuntimeException;

import org.fabric3.api.Fabric3RuntimeException;
import org.fabric3.spi.ObjectCreationException;

/**
 * Implementation of ComponentContext that delegates to a ComponentContextProvider.
 *
 * @version $Rev$ $Date$
 */
public class ComponentContextImpl implements ComponentContext {
    private final ComponentContextProvider component;

    public ComponentContextImpl(ComponentContextProvider component) {
        this.component = component;
    }

    public String getURI() {
        try {
            return component.getUri().toString();
        } catch (Fabric3RuntimeException e) {
            throw new ServiceRuntimeException(e.getMessage(), e);
        }
    }

    public <B, R extends CallableReference<B>> R cast(B target) throws IllegalArgumentException {
        try {
            return (R) component.cast(target);
        } catch (Fabric3RuntimeException e) {
            throw new ServiceRuntimeException(e.getMessage(), e);
        }
    }

    public <B> B getService(Class<B> businessInterface, String referenceName) {
        try {
            return component.getService(businessInterface, referenceName);
        } catch (ObjectCreationException e) {
            throw new ServiceRuntimeException(e.getMessage(), e);
        } catch (Fabric3RuntimeException e) {
            throw new ServiceRuntimeException(e.getMessage(), e);
        }
    }

    public <B> ServiceReference<B> getServiceReference(Class<B> businessInterface, String referenceName) {
        try {
            return component.getServiceReference(businessInterface, referenceName);
        } catch (Fabric3RuntimeException e) {
            throw new ServiceRuntimeException(e.getMessage(), e);
        }
    }

    public <B> B getProperty(Class<B> type, String propertyName) {
        try {
            return component.getProperty(type, propertyName);
        } catch (ObjectCreationException e) {
            throw new ServiceRuntimeException(e.getMessage(), e);
        } catch (Fabric3RuntimeException e) {
            throw new ServiceRuntimeException(e.getMessage(), e);
        }
    }

    public <B> ServiceReference<B> createSelfReference(Class<B> businessInterface) {
        return null;
    }

    public <B> ServiceReference<B> createSelfReference(Class<B> businessInterface, String serviceName) {
        return null;
    }

    public RequestContext getRequestContext() {
        return null;
    }
}
