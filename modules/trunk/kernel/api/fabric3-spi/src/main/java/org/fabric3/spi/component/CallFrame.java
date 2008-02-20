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

import org.osoa.sca.Conversation;

/**
 * Encapsulates information for a specific invocation that is made as part of a request entering the domain.
 *
 * @version $Revision$ $Date$
 */
public class CallFrame implements Serializable {
    private final String callerUri;
    private final Conversation callerConversation;
    private final Conversation conversation;

    /**
     * Constructor.
     *
     * @param callerUri            the URI the caller of the current service can be called back on
     * @param conversationId       the conversation id associated with the current forward service
     * @param callerConversationId the conversation id associated with the caller of the current service
     */
    public CallFrame(String callerUri, Conversation conversationId, Conversation callerConversationId) {
        this.callerUri = callerUri;
        this.conversation = conversationId;
        this.callerConversation = callerConversationId;
    }

    /**
     * Returns the URI of the callback of the current service.
     *
     * @return the URI of the callback of the current service
     */
    public String getCallbackUri() {
        return callerUri;
    }

    /**
     * Returns the conversation id associated with the forward invocation.
     *
     * @return the conversation id associated with the forward invocation
     */
    public Conversation getConversation() {
        return conversation;
    }

    /**
     * Returns the conversation id associated with the caller.
     *
     * @return the conversation id associated with the caller
     */
    public Conversation getCallerConversation() {
        return callerConversation;
    }

    /**
     * Performs a deep copy of the CallFrame.
     *
     * @return the copied frame
     */
    public CallFrame copy() {
        // data is immutable, return shallow copy
        return new CallFrame(callerUri, conversation, callerConversation);
    }
}
