/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 * --- Original Apache License ---
 *
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
package org.fabric3.pojo.component;

import java.util.Collection;

import org.oasisopen.sca.ComponentContext;
import org.oasisopen.sca.RequestContext;
import org.oasisopen.sca.ServiceReference;

/**
 * @version $Rev$ $Date$
 */
public class OASISPojoComponentContext implements ComponentContext {
    private final PojoComponent<?> component;
    private final OASISPojoRequestContext requestContext;

    public OASISPojoComponentContext(PojoComponent<?> component, OASISPojoRequestContext requestContext) {
        this.component = component;
        this.requestContext = requestContext;
    }

    public String getURI() {
        return component.getUri().toString();
    }

    public <B, R extends ServiceReference<B>> R cast(B target) throws IllegalArgumentException {
        return null;
    }

    public <B> B getService(Class<B> businessInterface, String referenceName) {
        return null;
    }

    public <B> ServiceReference<B> getServiceReference(Class<B> businessInterface, String referenceName) {
        return null;
    }

    public <B> Collection<B> getServices(Class<B> interfaze, String referenceName) {
        return null;
    }

    public <B> Collection<ServiceReference<B>> getServiceReferences(Class<B> interfaze, String referenceName) {
        return null;
    }

    public <B> B getProperty(Class<B> type, String propertyName) {
        return null;
    }

    public <B> ServiceReference<B> createSelfReference(Class<B> businessInterface) {
        return null;
    }

    public <B> ServiceReference<B> createSelfReference(Class<B> businessInterface, String serviceName) {
        return null;
    }

    public RequestContext getRequestContext() {
        return requestContext;
    }
}