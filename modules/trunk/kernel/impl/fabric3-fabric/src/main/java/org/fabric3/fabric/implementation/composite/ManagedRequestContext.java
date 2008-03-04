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
package org.fabric3.fabric.implementation.composite;

import javax.security.auth.Subject;

import org.osoa.sca.RequestContext;
import org.osoa.sca.ServiceReference;
import org.osoa.sca.CallableReference;

/**
 * @version $Rev$ $Date$
 */
public class ManagedRequestContext implements RequestContext {
    public Subject getSecuritySubject() {
        throw new UnsupportedOperationException();
    }

    public String getServiceName() {
        throw new UnsupportedOperationException();
    }

    public <B> ServiceReference<B> getServiceReference() {
        throw new UnsupportedOperationException();
    }

    public <CB> CB getCallback() {
        throw new UnsupportedOperationException();
    }

    public <CB> CallableReference<CB> getCallbackReference() {
        throw new UnsupportedOperationException();
    }
}
