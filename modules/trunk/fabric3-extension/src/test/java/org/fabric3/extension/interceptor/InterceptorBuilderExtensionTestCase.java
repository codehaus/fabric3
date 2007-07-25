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
package org.fabric3.extension.interceptor;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.fabric3.spi.builder.BuilderException;
import org.fabric3.spi.builder.interceptor.InterceptorBuilder;
import org.fabric3.spi.builder.interceptor.InterceptorBuilderRegistry;
import org.fabric3.spi.model.physical.PhysicalInterceptorDefinition;
import org.fabric3.spi.wire.Interceptor;

/**
 * @version $Rev$ $Date$
 */
public class InterceptorBuilderExtensionTestCase extends TestCase {

    public void testRegistration() {
        InterceptorBuilderRegistry registry = EasyMock.createMock(InterceptorBuilderRegistry.class);
        
        registry.register(EasyMock.eq(PhysicalInterceptorDefinition.class), EasyMock.isA(InterceptorBuilder.class));
        EasyMock.replay(registry);
        InterceptorBuilderExtension builder = new InterceptorBuilderExtension() {
            protected Class<PhysicalInterceptorDefinition> getInterceptorDefinitionClass() {
                return PhysicalInterceptorDefinition.class;
            }

            public Interceptor build(PhysicalInterceptorDefinition definition) throws BuilderException {
                return null;
            }
        };

        builder.setRegistry(registry);
        builder.init();
        EasyMock.verify(registry);
    }
}
