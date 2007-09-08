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
 * A registry of interceptor builders that performs a dispatch to a builder based on an interceptor definition class.
 *
 * @version $Rev$ $Date$
 */
public interface InterceptorBuilderRegistry {

    /**
     * Register a interceptor builder for the given interceptor definition class.
     *
     * @param clazz   the intyerceptor definition class associated with the builder
     * @param builder the builder to register
     */
    <PID extends PhysicalInterceptorDefinition> void register(Class<PID> clazz, InterceptorBuilder<PID, ?> builder);

    /**
     * Unregister a interceptor builder for the given QName
     *
     * @param clazz   the intyerceptor definition class associated with the builder
     */
    <PID extends PhysicalInterceptorDefinition> void unregister(Class<PID> clazz);

    /**
     * Dispatches to an interceptor builder matching the definition QName
     *
     * @param definition the interceptor definition to build from
     * @return an interceptor matching the given definition
     * @throws BuilderException if an error ocurrs during the dispatch or build
     */
    <PID extends PhysicalInterceptorDefinition> Interceptor build(PID definition) throws BuilderException;

}
