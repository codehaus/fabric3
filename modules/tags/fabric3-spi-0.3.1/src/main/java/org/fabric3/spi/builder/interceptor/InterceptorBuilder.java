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
package org.fabric3.spi.builder.interceptor;

import org.fabric3.spi.builder.BuilderException;
import org.fabric3.spi.model.physical.PhysicalInterceptorDefinition;
import org.fabric3.spi.wire.Interceptor;

/**
 * Implementations return an interceptor, creating one if necessary
 *
 * @version $Rev$ $Date$
 */
public interface InterceptorBuilder<PID extends PhysicalInterceptorDefinition, I extends Interceptor> {

    /**
     * Return an interceptor for the given interceptor definition metadata
     *
     * @param definition metadata used for returning an interceptor
     * @return the interceptor
     * @throws BuilderException if an error ocurrs returning the interceptor
     */
    I build(PID definition) throws BuilderException;
    
}
