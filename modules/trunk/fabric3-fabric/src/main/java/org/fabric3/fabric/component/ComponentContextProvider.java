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

import java.net.URI;

import org.osoa.sca.ComponentContext;
import org.osoa.sca.ServiceReference;
import org.osoa.sca.CallableReference;

import org.fabric3.spi.ObjectCreationException;

/**
 * Interface implemented by Component's that want to expose a ComponentContext.
 *
 * @version $Rev$ $Date$
 */
public interface ComponentContextProvider {
    ComponentContext getComponentContext();

    URI getUri();

    <B> B getService(Class<B> businessInterface, String referenceName) throws ObjectCreationException;

    <B> ServiceReference<B> getServiceReference(Class<B> businessInterface, String referenceName);

    <B> B getProperty(Class<B> type, String propertyName) throws ObjectCreationException;

    <B, R extends CallableReference<B>> R cast(B target);
}
