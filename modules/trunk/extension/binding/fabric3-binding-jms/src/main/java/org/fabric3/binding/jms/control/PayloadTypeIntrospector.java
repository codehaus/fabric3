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
package org.fabric3.binding.jms.control;

import org.fabric3.binding.jms.provision.PayloadType;
import org.fabric3.scdl.Operation;

/**
 * Introspects an operation's in parameters to determine the payload type.
 *
 * @version $Revision$ $Date$
 */
public interface PayloadTypeIntrospector {

    /**
     * Performs the introspection.
     *
     * @param operation the operation to introspect
     * @return the JMS message type
     * @throws JmsGenerationException if an error occurs introspecting the operation
     */
    <T> PayloadType introspect(Operation<T> operation) throws JmsGenerationException;

}
