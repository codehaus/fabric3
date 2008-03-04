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

import org.fabric3.spi.invocation.Message;

/**
 * Synchronous, around-style mediation associated with wire.
 *
 * @version $Rev$ $Date$
 */
public interface Interceptor {

    /**
     * Process a synchronous wire
     *
     * @param msg the request Message for the wire
     * @return the response Message from the wire
     */
    Message invoke(Message msg);

    /**
     * Sets the next interceptor
     *
     * @param next the next interceptor
     */
    void setNext(Interceptor next);

    /**
     * Returns the next interceptor or null
     *
     * @return he next interceptor or null
     */
    Interceptor getNext();

}
