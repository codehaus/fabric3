/*
 * Fabric3
 * Copyright (c) 2009-2012 Metaform Systems
 *
 * Fabric3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version, with the
 * following exception:
 *
 * Linking this software statically or dynamically with other
 * modules is making a combined work based on this software.
 * Thus, the terms and conditions of the GNU General Public
 * License cover the whole combination.
 *
 * As a special exception, the copyright holders of this software
 * give you permission to link this software with independent
 * modules to produce an executable, regardless of the license
 * terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided
 * that you also meet, for each linked independent module, the
 * terms and conditions of the license of that module. An
 * independent module is a module which is not derived from or
 * based on this software. If you modify this software, you may
 * extend this exception to your version of the software, but
 * you are not obligated to do so. If you do not wish to do so,
 * delete this exception statement from your version.
 *
 * Fabric3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the
 * GNU General Public License along with Fabric3.
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.spi.invocation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fabric3.api.SecuritySubject;

/**
 * Tracks information associated with a request as it is processed by the runtime. Requests originate at a domain boundary (e.g. a service bound to a
 * transport). As a request is processed by a component providing the service, invocations to other services in the domain may be made. State
 * associated with each invocation is encapsulated in a CallFrame and is added to the call stack associated with the WorkContext. When an invocation
 * completes, its CallFrame is removed from the stack.
 * <p/>
 * This implementation is <em>not</em> thread safe.
 *
 * @version $Rev$ $Date$
 */
public class WorkContext implements Serializable {
    private static final long serialVersionUID = 9108092492339191639L;
    private SecuritySubject subject;
    private List<CallFrame> callStack;
    private Map<String, Object> headers;

    public void setSubject(SecuritySubject subject) {
        this.subject = subject;
    }

    /**
     * Gets the subject associated with the current invocation.
     *
     * @return Subject associated with the current invocation.
     */
    public SecuritySubject getSubject() {
        return subject;
    }

    /**
     * Adds a CallFrame to the invocation stack.
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

    /**
     * Returns the header value for the given name associated with the current request context.
     *
     * @param type the expected header value type
     * @param name the header name
     * @return the header value or null if no header exists
     */
    public <T> T getHeader(Class<T> type, String name) {
        if (headers == null) {
            return null;
        }
        return type.cast(headers.get(name));
    }

    /**
     * Sets a header value for the current request context.
     *
     * @param name  the header name
     * @param value the header vale
     */
    public void setHeader(String name, Object value) {
        if (headers == null) {
            headers = new HashMap<String, Object>();
        }
        headers.put(name, value);
    }

    /**
     * Clears a header for the current request context.
     *
     * @param name the header name
     */
    public void removeHeader(String name) {
        if (headers == null) {
            return;
        }
        headers.remove(name);
    }

    /**
     * Returns all headers for the current request context.
     *
     * @return the map of header names and values
     */
    public Map<String, Object> getHeaders() {
        return headers;
    }

    public void addHeaders(Map<String, Object> newheaders) {
        if (headers == null) {
            headers = newheaders;
            return;
        }
        headers.putAll(newheaders);
    }
}
