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
package org.fabric3.fabric.integration.implementation;

import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Scope;
import org.fabric3.api.annotation.Resource;

import org.fabric3.pojo.processor.ImplementationProcessorService;
import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.pojo.instancefactory.Signature;
import static org.fabric3.scdl.Scope.COMPOSITE;
import org.fabric3.spi.component.ScopeRegistry;

import junit.framework.TestCase;
import org.fabric3.fabric.idl.java.JavaInterfaceProcessorRegistryImpl;
import org.fabric3.fabric.implementation.IntrospectionRegistryImpl;
import org.fabric3.fabric.implementation.processor.DestroyProcessor;
import org.fabric3.fabric.implementation.processor.ImplementationProcessorServiceImpl;
import org.fabric3.fabric.implementation.processor.InitProcessor;
import org.fabric3.fabric.implementation.processor.PropertyProcessor;
import org.fabric3.fabric.implementation.processor.ReferenceProcessor;
import org.fabric3.fabric.implementation.processor.ResourceProcessor;
import org.fabric3.fabric.implementation.processor.ScopeProcessor;
import org.fabric3.fabric.monitor.NullMonitorFactory;

import org.easymock.EasyMock;

/**
 * Sanity check of the <code>IntegrationRegistry</code> to verify operation with processors
 *
 * @version $Rev$ $Date$
 */
public class IntrospectionRegistryIntegrationTestCase extends TestCase {

    private IntrospectionRegistryImpl registry;

    public void testSimpleComponentTypeParsing() throws Exception {
        PojoComponentType type = new PojoComponentType(null);
        registry.introspect(Foo.class, type, null);
        assertEquals(new Signature(Foo.class.getMethod("init")), type.getInitMethod());
        assertEquals(new Signature(Foo.class.getMethod("destroy")), type.getDestroyMethod());
        assertEquals(COMPOSITE, type.getImplementationScope());
        assertEquals(Foo.class.getMethod("setBar", String.class), type.getProperties().get("bar").getMember());
        String targetName = type.getReferences().get("target").getMemberSite().getName();
        assertEquals("setTarget", targetName);
        assertEquals(Foo.class.getMethod("setResource", Foo.class), type.getResources().get("resource").getMember());
    }

    protected void setUp() throws Exception {
        super.setUp();
        ScopeRegistry scopeRegistry = EasyMock.createMock(ScopeRegistry.class);
        EasyMock.expect(scopeRegistry.getScope("COMPOSITE")).andStubReturn(org.fabric3.scdl.Scope.COMPOSITE);
        EasyMock.replay(scopeRegistry);

        registry = new IntrospectionRegistryImpl();
        registry.setMonitor(new NullMonitorFactory().getMonitor(IntrospectionRegistryImpl.Monitor.class));
        registry.registerProcessor(new DestroyProcessor());
        registry.registerProcessor(new InitProcessor());
        registry.registerProcessor(new ScopeProcessor(scopeRegistry));
        JavaInterfaceProcessorRegistryImpl interfaceProcessorRegistry = new JavaInterfaceProcessorRegistryImpl();
        ImplementationProcessorService service = new ImplementationProcessorServiceImpl(interfaceProcessorRegistry);
        registry.registerProcessor(new PropertyProcessor(service));
        registry.registerProcessor(new ReferenceProcessor(interfaceProcessorRegistry));
        registry.registerProcessor(new ResourceProcessor());
    }

    @Scope("COMPOSITE")
    private static class Foo {
        protected Foo target;
        protected String bar;
        protected Foo resource;
        private boolean initialized;
        private boolean destroyed;


        @Init
        public void init() {
            if (initialized) {
                fail();
            }
            initialized = true;
        }

        @Destroy
        public void destroy() {
            if (destroyed) {
                fail();
            }
            destroyed = true;
        }

        public Foo getTarget() {
            return target;
        }

        @Reference
        public void setTarget(Foo target) {
            this.target = target;
        }

        public String getBar() {
            return bar;
        }

        @Property
        public void setBar(String bar) {
            this.bar = bar;
        }

        @Resource
        public void setResource(Foo resource) {
            this.resource = resource;
        }

    }
}
