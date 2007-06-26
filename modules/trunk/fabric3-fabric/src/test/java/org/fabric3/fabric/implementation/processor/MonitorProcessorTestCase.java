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
package org.fabric3.fabric.implementation.processor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import org.fabric3.pojo.processor.ConstructorDefinition;
import org.fabric3.pojo.processor.JavaMappedProperty;
import org.fabric3.pojo.processor.PojoComponentType;
import org.fabric3.pojo.processor.IllegalPropertyException;
import org.fabric3.spi.SingletonObjectFactory;

import junit.framework.TestCase;
import org.fabric3.api.annotation.Monitor;
import org.fabric3.fabric.idl.java.JavaInterfaceProcessorRegistryImpl;
import org.fabric3.host.monitor.MonitorFactory;

import org.easymock.EasyMock;

/**
 * @version $Rev$ $Date$
 */
public class MonitorProcessorTestCase extends TestCase {

    private MonitorProcessor processor;
    private MonitorFactory monitorFactory;

    public void testSetter() throws Exception {
        PojoComponentType type =
            new PojoComponentType();
        Method method = Foo.class.getMethod("setMonitor", Foo.class);
        EasyMock.expect(monitorFactory.getMonitor(EasyMock.eq(Foo.class))).andReturn(null);
        EasyMock.replay(monitorFactory);
        processor.visitMethod(method, type, null);
        Map<String, JavaMappedProperty<?>> properties = type.getProperties();
        assertTrue(properties.get("monitor").getDefaultValueFactory() instanceof SingletonObjectFactory);
        EasyMock.verify(monitorFactory);
    }


    public void testBadSetter() throws Exception {
        PojoComponentType type =
            new PojoComponentType();
        Method method = BadMonitor.class.getMethod("setMonitor");
        try {
            processor.visitMethod(method, type, null);
            fail();
        } catch (IllegalPropertyException e) {
            // expected
        }
    }

    public void testField() throws Exception {
        PojoComponentType type =
            new PojoComponentType();
        Field field = Foo.class.getDeclaredField("bar");
        EasyMock.expect(monitorFactory.getMonitor(EasyMock.eq(Foo.class))).andReturn(null);
        EasyMock.replay(monitorFactory);
        processor.visitField(field, type, null);
        Map<String, JavaMappedProperty<?>> properties = type.getProperties();
        assertTrue(properties.get("bar").getDefaultValueFactory() instanceof SingletonObjectFactory);
        EasyMock.verify(monitorFactory);
    }

    public void testConstructor() throws Exception {
        PojoComponentType type =
            new PojoComponentType();
        Constructor<Bar> ctor = Bar.class.getConstructor(BazMonitor.class);
        EasyMock.expect(monitorFactory.getMonitor(EasyMock.eq(BazMonitor.class))).andReturn(null);
        EasyMock.replay(monitorFactory);
        processor.visitConstructor(ctor, type, null);
        Map<String, JavaMappedProperty<?>> properties = type.getProperties();
        assertTrue(
            properties.get(BazMonitor.class.getName()).getDefaultValueFactory() instanceof SingletonObjectFactory);
        EasyMock.verify(monitorFactory);
    }

    /**
     * Verifies calling the monitor processor to evaluate a constructor can be done after a property parameter is
     * processed
     */
    public void testConstructorAfterProperty() throws Exception {
        PojoComponentType type =
            new PojoComponentType();
        Constructor<Bar> ctor = Bar.class.getConstructor(String.class, BazMonitor.class);
        EasyMock.expect(monitorFactory.getMonitor(EasyMock.eq(BazMonitor.class))).andReturn(null);
        EasyMock.replay(monitorFactory);
        ConstructorDefinition<Bar> definition = new ConstructorDefinition<Bar>(ctor);
        JavaMappedProperty prop = new JavaMappedProperty();
        definition.getInjectionNames().add("prop");
        type.setConstructorDefinition(definition);
        type.getProperties().put("prop", prop);
        processor.visitConstructor(ctor, type, null);
        Map<String, JavaMappedProperty<?>> properties = type.getProperties();
        assertEquals(BazMonitor.class.getName(), definition.getInjectionNames().get(1));
        assertEquals(2, type.getProperties().size());
        String name = BazMonitor.class.getName();
        assertTrue(properties.get(name).getDefaultValueFactory() instanceof SingletonObjectFactory);
        EasyMock.verify(monitorFactory);
    }

    /**
     * Verifies calling the monitor processor to evaluate a constructor can be done before a property parameter is
     * processed
     */
    public void testConstructorBeforeProperty() throws Exception {
        PojoComponentType type =
            new PojoComponentType();
        Constructor<Bar> ctor = Bar.class.getConstructor(String.class, BazMonitor.class);
        EasyMock.expect(monitorFactory.getMonitor(EasyMock.eq(BazMonitor.class))).andReturn(null);
        EasyMock.replay(monitorFactory);
        processor.visitConstructor(ctor, type, null);
        Map<String, JavaMappedProperty<?>> properties = type.getProperties();
        ConstructorDefinition definition = type.getConstructorDefinition();
        assertEquals(2, definition.getInjectionNames().size());
        assertEquals(BazMonitor.class.getName(), definition.getInjectionNames().get(1));
        String name = BazMonitor.class.getName();
        assertTrue(properties.get(name).getDefaultValueFactory() instanceof SingletonObjectFactory);
        EasyMock.verify(monitorFactory);
    }

    protected void setUp() throws Exception {
        super.setUp();
        monitorFactory = EasyMock.createMock(MonitorFactory.class);
        JavaInterfaceProcessorRegistryImpl registry = new JavaInterfaceProcessorRegistryImpl();
        ImplementationProcessorServiceImpl processor = new ImplementationProcessorServiceImpl(registry);
        this.processor = new MonitorProcessor(monitorFactory, processor);
    }

    private class Foo {

        @Monitor
        protected Foo bar;

        @Monitor
        public void setMonitor(Foo foo) {
        }
    }


    private class BadMonitor {

        @Monitor
        public void setMonitor() {
        }
    }

    private interface BazMonitor {

    }

    private static class Bar {

        public Bar(@Monitor BazMonitor monitor) {
        }

        public Bar(String prop, @Monitor BazMonitor monitor) {
        }
    }
}
