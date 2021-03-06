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

import org.osoa.sca.ServiceRuntimeException;

/**
 * Denotes a runtime exception thrown during an invocation over a wire
 *
 * @version $Rev$ $Date$
 */
public class InvocationRuntimeException extends ServiceRuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = -5262124031513496306L;

    public InvocationRuntimeException() {
        super();
    }

    public InvocationRuntimeException(String message) {
        super(message);
    }

    public InvocationRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvocationRuntimeException(Throwable cause) {
        super(cause);
    }

}
