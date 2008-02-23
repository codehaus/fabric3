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

import org.osoa.sca.annotations.Property;
import org.easymock.EasyMock;

import org.fabric3.pojo.scdl.PojoComponentType;

import junit.framework.TestCase;
import org.fabric3.introspection.impl.contract.DefaultContractProcessor;
import org.fabric3.introspection.impl.DefaultIntrospectionHelper;
import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.TypeMapping;
import org.fabric3.scdl.Signature;

/**
 * Verifies the constructor processor works when parameters are marked with custom extension annotations
 *
 * @version $Rev$ $Date$
 */
public class ConstructorProcessorExtensibilityTestCase extends TestCase {
    private ConstructorProcessor processor;
    private IntrospectionContext context;

    public void testProcessFirst() throws Exception {
        PojoComponentType type = new PojoComponentType(null);
        Constructor<Foo> ctor1 = Foo.class.getConstructor(String.class, String.class);
        processor.visitConstructor(ctor1, type, context);
        assertTrue(type.getProperties().containsKey("foo"));
    }

    /**
     * Verifies the constructor processor throws an exception if another processor has selected a constructor.
     *
     * @throws Exception
     */
    public void testProcessLast() throws Exception {
        PojoComponentType type = new PojoComponentType(null);
        Constructor<Foo> ctor = Foo.class.getConstructor(String.class, String.class);
        type.setConstructor(new Signature(ctor));
        try {
            processor.visitConstructor(ctor, type, context);
            fail();
        } catch (DuplicateConstructorException e) {
        }
    }


    protected void setUp() throws Exception {
        super.setUp();
        DefaultIntrospectionHelper helper = new DefaultIntrospectionHelper();
        DefaultContractProcessor contractProcessor = new DefaultContractProcessor(helper);
        processor = new ConstructorProcessor(new ImplementationProcessorServiceImpl(contractProcessor, helper));

        context = EasyMock.createMock(IntrospectionContext.class);
        TypeMapping typeMapping = new TypeMapping();
        EasyMock.expect(context.getTypeMapping()).andStubReturn(typeMapping);
        EasyMock.replay(context);
    }

    private @interface Bar {

    }

    private static class Foo {
        @org.osoa.sca.annotations.Constructor
        public Foo(@Property(name = "foo") String foo, @Bar String bar) {

        }
    }


}
