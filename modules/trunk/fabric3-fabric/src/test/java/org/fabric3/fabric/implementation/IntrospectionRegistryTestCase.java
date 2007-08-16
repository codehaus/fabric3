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
package org.fabric3.fabric.implementation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.pojo.processor.ImplementationProcessor;
import org.fabric3.pojo.processor.PojoComponentType;

import junit.framework.TestCase;
import org.fabric3.fabric.implementation.IntrospectionRegistryImpl.Monitor;
import org.fabric3.fabric.monitor.NullMonitorFactory;

import org.easymock.EasyMock;

/**
 * @version $Rev$ $Date$
 */
public class IntrospectionRegistryTestCase extends TestCase {

    private Monitor monitor;

    public void testRegister() throws Exception {
        IntrospectionRegistryImpl registry = new IntrospectionRegistryImpl(monitor);
        ImplementationProcessor processor = EasyMock.createNiceMock(ImplementationProcessor.class);
        registry.registerProcessor(processor);
    }

    public void testUnegister() throws Exception {
        IntrospectionRegistryImpl registry = new IntrospectionRegistryImpl(monitor);
        ImplementationProcessor processor = EasyMock.createNiceMock(ImplementationProcessor.class);
        registry.registerProcessor(processor);
        registry.unregisterProcessor(processor);
    }

    @SuppressWarnings("unchecked")
    public void testWalk() throws Exception {
        IntrospectionRegistryImpl registry = new IntrospectionRegistryImpl(monitor);
        ImplementationProcessor processor = EasyMock.createMock(ImplementationProcessor.class);
        processor.visitClass(
            EasyMock.eq(Bar.class),
            EasyMock.isA(PojoComponentType.class),
            EasyMock.isA(LoaderContext.class));
        processor.visitConstructor(
            EasyMock.isA(Constructor.class),
            EasyMock.isA(PojoComponentType.class),
            EasyMock.isA(LoaderContext.class));
        processor.visitMethod(
            EasyMock.isA(Method.class),
            EasyMock.isA(PojoComponentType.class),
            EasyMock.isA(LoaderContext.class));
        processor.visitField(
            EasyMock.isA(Field.class),
            EasyMock.isA(PojoComponentType.class),
            EasyMock.isA(LoaderContext.class));
        processor.visitSuperClass(
            EasyMock.isA(Class.class),
            EasyMock.isA(PojoComponentType.class),
            EasyMock.isA(LoaderContext.class));
        processor.visitEnd(
            EasyMock.isA(Class.class),
            EasyMock.isA(PojoComponentType.class),
            EasyMock.isA(LoaderContext.class));

        //   mock.expects(once()).method("visitClass");
//        mock.expects(once()).method("visitMethod");
//        mock.expects(once()).method("visitField");
//        mock.expects(once()).method("visitConstructor");
//        mock.expects(once()).method("visitSuperClass");
//        mock.expects(once()).method("visitEnd");
        EasyMock.replay(processor);
        registry.registerProcessor(processor);
        registry.introspect(
            Bar.class,
            new PojoComponentType(null),
            EasyMock.createNiceMock(LoaderContext.class));
        EasyMock.verify(processor);
    }


    protected void setUp() throws Exception {
        super.setUp();
        monitor = new NullMonitorFactory().getMonitor(Monitor.class);
    }

    private class Baz {

    }

    private class Bar extends Baz {

        protected String bar;

        public Bar() {
        }

        public void bar() {
        }

    }

}
