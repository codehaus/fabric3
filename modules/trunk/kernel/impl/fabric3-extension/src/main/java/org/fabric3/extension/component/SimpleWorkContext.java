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
package org.fabric3.extension.component;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import javax.security.auth.Subject;

import org.fabric3.scdl.Scope;
import org.fabric3.spi.component.WorkContext;
import org.fabric3.spi.component.CallFrame;

/**
 * A simple WorkContext implementation that provides support for storing work context information. The implementation is <em>not</em> thread safe.
 *
 * @version $Rev$ $Date$
 */
public class SimpleWorkContext implements WorkContext {
    private final Map<Scope<?>, Object> scopeIdentifiers = new HashMap<Scope<?>, Object>();
    private Subject subject;
    private List<CallFrame> callStack;

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public Subject getSubject() {
        return subject;
    }

    public <T> T getScopeIdentifier(Scope<T> scope) {
        return scope.getIdentifierType().cast(scopeIdentifiers.get(scope));
    }

    public <T> void setScopeIdentifier(Scope<T> scope, T identifier) {
        if (identifier == null) {
            scopeIdentifiers.remove(scope);
        } else {
            scopeIdentifiers.put(scope, identifier);
        }
    }

    public void addCallFrame(CallFrame frame) {
        if (callStack == null) {
            callStack = new ArrayList<CallFrame>();
        }
        callStack.add(frame);
    }

    public void addCallFrames(List<CallFrame> frames) {
        if (callStack == null) {
            callStack = frames;
            return;
        }
        callStack.addAll(frames);
    }

    public CallFrame popCallFrame() {
        if (callStack == null || callStack.isEmpty()) {
            return null;
        }
        return callStack.remove(callStack.size() - 1);
    }

    public CallFrame peekCallFrame() {
        if (callStack == null || callStack.isEmpty()) {
            return null;
        }
        return callStack.get(callStack.size() - 1);
    }

    public List<CallFrame> getCallFrameStack() {
        // return a live list to avoid creation of a non-modifiable collection
        return callStack;
    }
}
