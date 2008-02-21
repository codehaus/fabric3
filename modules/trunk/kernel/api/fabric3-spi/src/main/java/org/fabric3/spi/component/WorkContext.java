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

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import javax.security.auth.Subject;

import org.fabric3.scdl.Scope;

/**
 * Implementations track information associated with a request as it is processed by the runtime. The implementation is <em>not</em> thread safe.
 *
 * @version $Rev$ $Date$
 */
public class WorkContext {
    private final Map<Scope<?>, Object> scopeIdentifiers = new HashMap<Scope<?>, Object>();
    private Subject subject;
    private List<CallFrame> callStack;

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    /**
     * Gets the subject associated with the current invocation.
     *
     * @return Subject associated with the current invocation.
     */
    public Subject getSubject() {
        return subject;
    }

    /**
     * Returns the identifier currently associated with the supplied scope.
     *
     * @param scope the scope whose identifier should be returned
     * @return the scope identifier
     */
    public <T> T getScopeIdentifier(Scope<T> scope) {
        return scope.getIdentifierType().cast(scopeIdentifiers.get(scope));
    }

    /**
     * Sets the identifier associated with a scope.
     *
     * @param scope      the scope whose identifier we are setting
     * @param identifier the identifier for that scope
     */
    public <T> void setScopeIdentifier(Scope<T> scope, T identifier) {
        if (identifier == null) {
            scopeIdentifiers.remove(scope);
        } else {
            scopeIdentifiers.put(scope, identifier);
        }
    }

    /**
     * Adds a CallFrame to the internal CallFrame stack. CallFrames track information related to an invocation that is made as part of processing a
     * request. As an invocation is made, a CallFrame is added to the stack. CallFrames are removed fromt he stack when the invocation returns.
     *
     * @param frame the CallFrame to add
     */
    public void addCallFrame(CallFrame frame) {
        if (callStack == null) {
            callStack = new ArrayList<CallFrame>();
        }
        callStack.add(frame);
    }

    /**
     * Adds a collection of CallFrames to the internal CallFrame stack.
     *
     * @param frames the collection of CallFrames to add
     */
    public void addCallFrames(List<CallFrame> frames) {
        if (callStack == null) {
            callStack = frames;
            return;
        }
        callStack.addAll(frames);
    }

    /**
     * Removes and returns the CallFrame associated with the previous request from the internal stack.
     *
     * @return the CallFrame.
     */
    public CallFrame popCallFrame() {
        if (callStack == null || callStack.isEmpty()) {
            return null;
        }
        return callStack.remove(callStack.size() - 1);
    }

    /**
     * Returns but does not remove the CallFrame associated with the previous request from the internal stack.
     *
     * @return the CallFrame.
     */
    public CallFrame peekCallFrame() {
        if (callStack == null || callStack.isEmpty()) {
            return null;
        }
        return callStack.get(callStack.size() - 1);
    }

    /**
     * Returns the CallFrame stack.
     *
     * @return the CallFrame stack
     */
    public List<CallFrame> getCallFrameStack() {
        // return a live list to avoid creation of a non-modifiable collection
        return callStack;
    }

}
