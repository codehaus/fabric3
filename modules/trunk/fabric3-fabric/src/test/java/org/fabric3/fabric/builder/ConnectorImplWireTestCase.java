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
package org.fabric3.fabric.builder;

import java.net.URI;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.fabric.component.ComponentManagerImpl;
import org.fabric3.fabric.idl.java.JavaInterfaceProcessorRegistryImpl;
import org.fabric3.spi.builder.component.WireAttacherRegistry;
import org.fabric3.spi.component.AtomicComponent;
import org.fabric3.spi.component.ComponentManager;
import org.fabric3.spi.idl.java.JavaInterfaceProcessorRegistry;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.model.physical.PhysicalWireDefinition;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.model.type.ServiceContract;

/**
 * @version $Rev$ $Date$
 */
public class ConnectorImplWireTestCase extends TestCase {
    private static final URI SOURCE_URI = URI.create("source");
    private static final URI TARGET_URI = URI.create("target");
    private ComponentManager manager;
    private Connector connector;
    private ServiceContract<?> contract;

    public void testConnectWireDefinition() throws Exception {
        AtomicComponent source = EasyMock.createMock(AtomicComponent.class);
        EasyMock.expect(source.getUri()).andReturn(SOURCE_URI).atLeastOnce();
        EasyMock.replay(source);
        manager.register(source);

        AtomicComponent target = EasyMock.createMock(AtomicComponent.class);
        EasyMock.expect(target.getUri()).andReturn(TARGET_URI).atLeastOnce();
        EasyMock.replay(target);
        manager.register(target);

        PhysicalWireDefinition definition = new PhysicalWireDefinition();
        PhysicalWireSourceDefinition pwsd = new PhysicalWireSourceDefinition();
        pwsd.setUri(SOURCE_URI);
        definition.setSource(pwsd);
        PhysicalWireTargetDefinition pwtd = new PhysicalWireTargetDefinition();
        pwtd.setUri(TARGET_URI);
        definition.setTarget(pwtd);
        PhysicalOperationDefinition op = new PhysicalOperationDefinition();
        definition.addOperation(op);
        connector.connect(definition);
        EasyMock.verify(source);
        EasyMock.verify(target);
    }


    protected void setUp() throws Exception {
        super.setUp();
        manager = new ComponentManagerImpl();
        WireAttacherRegistry attacherRegistry = EasyMock.createNiceMock(WireAttacherRegistry.class);
        EasyMock.replay(attacherRegistry);
        connector = new ConnectorImpl(null, attacherRegistry);
        JavaInterfaceProcessorRegistry registry = new JavaInterfaceProcessorRegistryImpl();
        contract = registry.introspect(Foo.class);
    }

    private interface Foo {
        void bar();
    }

}
