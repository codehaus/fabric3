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
package org.fabric3.fabric.wire;

import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.wire.InvocationChain;

/**
 * Default implementation of an invocation chain
 *
 * @version $Rev$ $Date$
 */
public class InvocationChainImpl implements InvocationChain {
    protected PhysicalOperationDefinition physicalOperation;
    protected Interceptor interceptorChainHead;
    protected Interceptor interceptorChainTail;

    public InvocationChainImpl(PhysicalOperationDefinition operation) {
        this.physicalOperation = operation;
    }

    public PhysicalOperationDefinition getPhysicalOperation() {
        return physicalOperation;
    }

    public void addInterceptor(Interceptor interceptor) {
        if (interceptorChainHead == null) {
            interceptorChainHead = interceptor;
        } else {
            interceptorChainTail.setNext(interceptor);
        }
        interceptorChainTail = interceptor;
    }

    public void addInterceptor(int index, Interceptor interceptor) {
        int i = 0;
        Interceptor next = interceptorChainHead;
        Interceptor prev = null;
        while (next != null && i < index) {
            prev = next;
            next = next.getNext();
            i++;
        }
        if (i == index) {
            if (prev != null) {
                prev.setNext(interceptor);
            } else {
                interceptorChainHead = interceptor;
            }
            interceptor.setNext(next);
            if (next == null) {
                interceptorChainTail = interceptor;
            }
        } else {
            throw new ArrayIndexOutOfBoundsException(index);
        }
    }

    public Interceptor getHeadInterceptor() {
        return interceptorChainHead;
    }

    public Interceptor getTailInterceptor() {
        return interceptorChainTail;
    }

}
