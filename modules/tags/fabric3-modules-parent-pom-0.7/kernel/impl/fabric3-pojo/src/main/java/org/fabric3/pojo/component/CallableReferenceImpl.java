/*
 * Fabric3
 * Copyright � 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the �License�), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an �as is� basis,
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

import org.osoa.sca.CallableReference;
import org.osoa.sca.Conversation;
import org.osoa.sca.ServiceRuntimeException;

import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.ObjectCreationException;

/**
 * Base class for implementations of service and callback references.
 * 
 * @version $Rev$ $Date$
 * @param <B> the type of the business interface
 */
public abstract class CallableReferenceImpl<B> implements CallableReference<B> {
    private final Class<B> businessInterface;
    private final ObjectFactory<B> factory;

    protected CallableReferenceImpl(Class<B> businessInterface, ObjectFactory<B> factory) {
        this.businessInterface = businessInterface;
        this.factory = factory;
    }

    public B getService() {
        try {
            return factory.getInstance();
        } catch (ObjectCreationException e) {
            throw new ServiceRuntimeException(e.getMessage(), e);
        }
    }

    public Class<B> getBusinessInterface() {
        return businessInterface;
    }

    public boolean isConversational() {
        return false;
    }

    public Conversation getConversation() {
        return null;
    }

    public Object getCallbackID() {
        return null;
    }
}
