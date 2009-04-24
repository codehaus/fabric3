/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 * --- Original Apache License ---
 *
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
package org.fabric3.fabric.builder;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.spi.builder.interceptor.InterceptorBuilder;
import org.fabric3.spi.model.physical.PhysicalInterceptorDefinition;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.model.physical.PhysicalWireDefinition;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.wire.Wire;

/**
 * @version $Rev$ $Date$
 */
public class ConnectorImplTestCase extends TestCase {
    private ConnectorImpl connector;
    private PhysicalWireDefinition definition;
    private PhysicalOperationDefinition operation;
    private PhysicalOperationDefinition callback;
    private Map<Class<? extends PhysicalInterceptorDefinition>, InterceptorBuilder<?>> builders;

    public void testCreateWire() throws Exception {
        Wire wire = connector.createWire(definition);
        assertEquals(2, wire.getInvocationChains().size());
    }

    @SuppressWarnings({"unchecked"})
    public void testDispatchToBuilder() throws Exception {
        InterceptorBuilder builder = EasyMock.createMock(InterceptorBuilder.class);
        EasyMock.expect(builder.build(EasyMock.isA(PhysicalInterceptorDefinition.class))).andReturn(null).times(2);
        EasyMock.replay(builder);
        builders.put(PhysicalInterceptorDefinition.class, builder);

        PhysicalInterceptorDefinition interceptorDefinition = new PhysicalInterceptorDefinition();
        operation.addInterceptor(interceptorDefinition);
        callback.addInterceptor(interceptorDefinition);

        connector.createWire(definition);
        EasyMock.verify(builder);
    }

    protected void setUp() throws Exception {
        super.setUp();
        connector = new ConnectorImpl();
        builders = new HashMap<Class<? extends PhysicalInterceptorDefinition>, InterceptorBuilder<?>>();
        connector.setInterceptorBuilders(builders);

        PhysicalWireSourceDefinition sourceDefinition = new PhysicalWireSourceDefinition();
        sourceDefinition.setUri(URI.create("source"));
        PhysicalWireTargetDefinition targetDefinition = new PhysicalWireTargetDefinition();
        targetDefinition.setUri(URI.create("target"));
        Set<PhysicalOperationDefinition> operations = new HashSet<PhysicalOperationDefinition>();
        definition = new PhysicalWireDefinition(sourceDefinition, targetDefinition, operations);
        operation = new PhysicalOperationDefinition();
        operation.setName("operation");
        definition.addOperation(operation);
        callback = new PhysicalOperationDefinition();
        callback.setName("callback");
        callback.setCallback(true);
        definition.addOperation(callback);
    }
}
