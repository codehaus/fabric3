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

import javax.xml.namespace.QName;

import org.osoa.sca.annotations.Reference;

import org.fabric3.extension.interceptor.InterceptorBuilderExtension;
import org.fabric3.spi.Constants;
import org.fabric3.spi.builder.BuilderException;
import org.fabric3.spi.model.physical.PhysicalInterceptorDefinition;
import org.fabric3.spi.services.work.WorkScheduler;
import org.fabric3.spi.wire.Interceptor;

/**
 * Creates a non-blocking interceptor
 *
 * @version $Rev$ $Date$
 */
public class NonBlockingInterceptorBuilder extends InterceptorBuilderExtension {
    public static final QName QNAME = new QName(Constants.FABRIC3_SYSTEM_NS, "nonblocking");
    private WorkScheduler scheduler;

    public NonBlockingInterceptorBuilder(@Reference(required = true)WorkScheduler scheduler) {
        this.scheduler = scheduler;
    }

    public Interceptor build(PhysicalInterceptorDefinition definition) throws BuilderException {
        return new NonBlockingInterceptor(scheduler);
    }

    protected QName getName() {
        return QNAME;
    }
}
