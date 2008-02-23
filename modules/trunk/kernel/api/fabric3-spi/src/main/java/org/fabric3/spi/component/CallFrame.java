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

import java.io.Serializable;

/**
 * Encapsulates information for a specific invocation that is made as part of a request entering the domain. Requests may have multiple associated
 * invocations as component implementations may invoke services on other components as a request is processed.
 *
 * @version $Revision$ $Date$
 */
public class CallFrame implements Serializable {
    private static final long serialVersionUID = -6108279393891496098L;
    private String callbackUri;
    private Object forwardCorrelationId;
    private Object callerCorrelationId;

    /**
     * Constructor.
     *
     * @param callbackUri          the URI the caller of the current service can be called back on
     * @param forwardCorrelationId the key used to correlate the forward invocation with the target component implementation instance. For stateless
     *                             targets, the id may be null.
     * @param callerCorrelationId  the key used to correlate callback invocations with the callback component implementation instance. For stateless
     *                             targets, the id may be null.
     */
    public CallFrame(String callbackUri, Object forwardCorrelationId, Object callerCorrelationId) {
        this.callbackUri = callbackUri;
        this.forwardCorrelationId = forwardCorrelationId;
        this.callerCorrelationId = callerCorrelationId;
    }

    /**
     * Convenience constructor for invocations to statefull unidirectional services.
     *
     * @param forwardCorrelationId the key used to correlate the forward invocation with the target component implementation instance. For stateless
     */
    public CallFrame(Object forwardCorrelationId) {
        this(null, forwardCorrelationId, null);
    }

    /**
     * Returns the URI of the callback service for the current invocation.
     *
     * @return the callback service URI or null if the invocation is to a unidirectional service.
     */
    public String getCallbackUri() {
        return callbackUri;
    }

    /**
     * Returns the key used to correlate the forward invocation with the target component implementation instance or null if the target is stateless.
     *
     * @param type the correlation id type.
     * @return the correlation id or null.
     */
    public <T> T getForwardCorrelationId(Class<T> type) {
        return type.cast(forwardCorrelationId);
    }

    /**
     * Returns the key used to correlate the forward invocation with the callback component implementation instance or null if the callback target is
     * stateless.
     *
     * @param type the correlation id type.
     * @return the correlation id or null.
     */
    public <T> T getCallbackCorrelationId(Class<T> type) {
        return type.cast(callerCorrelationId);
    }

    /**
     * Performs a deep copy of the CallFrame.
     *
     * @return the copied frame
     */
    public CallFrame copy() {
        // data is immutable, return shallow copy
        return new CallFrame(callbackUri, forwardCorrelationId, callerCorrelationId);
    }
}
