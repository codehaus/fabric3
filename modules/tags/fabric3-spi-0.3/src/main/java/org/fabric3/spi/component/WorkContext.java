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
package org.fabric3.spi.component;

import java.util.LinkedList;

import org.fabric3.scdl.Scope;
import org.fabric3.spi.wire.Wire;

/**
 * Implementations track information associated with a request as it is processed by the runtime
 *
 * @version $Rev$ $Date$
 */
public interface WorkContext {

    /**
     * Returns the identifier currently associated with the supplied scope.
     *
     * @param scope the scope whose identifier should be returned
     * @return the scope identifier
     */
    <T> T getScopeIdentifier(Scope<T> scope);

    /**
     * Sets the identifier associated with a scope.
     *
     * @param scope      the scope whose identifier we are setting
     * @param identifier the identifier for that scope
     */
    <T> void setScopeIdentifier(Scope<T> scope, T identifier);

    /**
     * Returns an ordered list of callback wures for the current context. Ordering is based on the sequence of service
     * invocations for collocated components
     *
     * @return the current list of callback wires
     */
    LinkedList<Wire> getCallbackWires();

    /**
     * Sets an ordered list of callback wires for the current context. Ordering is based on the sequence of service
     * invocations for collocated components
     */
    void setCallbackWires(LinkedList<Wire> wires);

    /**
     * Returns the correlation id for the current invocation or null if not available. Transports may use correlation
     * ids for message routing.
     *
     * @return the correlation id for the current invocation or null
     */
    Object getCorrelationId();

    /**
     * Sets the correlation id for the current invocation. Transports may use correlation ids for message routing.
     *
     * @param id the correlation id
     */
    void setCorrelationId(Object id);

    /**
     * Removes and returns the name of the last remotable service to handle the current request
     *
     * @return the name of the last remotable service to handle the current request or null
     */
    String popServiceName();

    /**
     * Returns the name of the last remotable service to handle the current request
     *
     * @return the name of the last remotable service to handle the current request or null
     */
    String getCurrentServiceName();

    /**
     * Adds the name of the last remotable service to handle the current request
     *
     * @param name the name of the last remotable service to handle the current request or null
     */
    void pushServiceName(String name);

    /**
     * Clears the stack of current service names
     */
    void clearServiceNames();
}
