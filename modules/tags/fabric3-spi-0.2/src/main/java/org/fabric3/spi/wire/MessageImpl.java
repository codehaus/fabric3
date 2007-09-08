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
package org.fabric3.spi.wire;

import org.fabric3.spi.component.WorkContext;

/**
 * The default implementation of a message flowed through a wire during an invocation
 *
 * @version $Rev $Date
 */
public class MessageImpl implements Message {
    private Object body;
    private boolean isFault;
    private WorkContext workContext;
    private Wire wire;

    public MessageImpl() {
    }

    public MessageImpl(Object body, boolean isFault, WorkContext workContext, Wire wire) {
        this.body = body;
        this.isFault = isFault;
        this.workContext = workContext;
        this.wire = wire;
    }

    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.isFault = false;
        this.body = body;
    }

    public void setBodyWithFault(Object fault) {
        this.isFault = true;
        this.body = fault;
    }

    public boolean isFault() {
        return isFault;
    }

    public WorkContext getWorkContext() {
        return workContext;
    }

    public void setWorkContext(WorkContext workContext) {
        this.workContext = workContext;
    }

    public Wire getWire() {
        return wire;
    }

    public void setWire(Wire wire) {
        this.wire = wire;
    }
}
