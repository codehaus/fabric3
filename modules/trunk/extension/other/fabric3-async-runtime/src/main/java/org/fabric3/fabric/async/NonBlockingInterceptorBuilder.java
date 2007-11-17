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
package org.fabric3.fabric.async;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.extension.interceptor.InterceptorBuilderExtension;
import org.fabric3.spi.builder.BuilderException;
import org.fabric3.spi.services.work.WorkScheduler;

/**
 * Creates a non-blocking interceptor
 *
 * @version $Rev$ $Date$
 */
@EagerInit      
public class NonBlockingInterceptorBuilder
        extends InterceptorBuilderExtension<NonBlockingInterceptorDefinition, NonBlockingInterceptor> {
    private WorkScheduler scheduler;

    public NonBlockingInterceptorBuilder(@Reference(required = true)WorkScheduler scheduler) {
        this.scheduler = scheduler;
    }

    public NonBlockingInterceptor build(NonBlockingInterceptorDefinition definition) throws BuilderException {
        return new NonBlockingInterceptor(scheduler);
    }

    @Override
    protected Class<NonBlockingInterceptorDefinition> getInterceptorDefinitionClass() {
        return NonBlockingInterceptorDefinition.class;
    }
}
