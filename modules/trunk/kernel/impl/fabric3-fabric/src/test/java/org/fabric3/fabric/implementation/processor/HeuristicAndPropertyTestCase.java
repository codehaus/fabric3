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

import org.fabric3.pojo.scdl.ConstructorDefinition;
import org.fabric3.pojo.scdl.PojoComponentType;

import junit.framework.TestCase;
import org.fabric3.fabric.idl.java.JavaInterfaceProcessorRegistryImpl;

/**
 * @version $Rev$ $Date$
 */
public class HeuristicAndPropertyTestCase extends TestCase {

    private PropertyProcessor propertyProcessor;
    private HeuristicPojoProcessor heuristicProcessor;

    /**
     * Verifies the property and heuristic processors don't collide
     */
    @SuppressWarnings("unchecked")
    public void testPropertyProcessorWithHeuristicProcessor() throws Exception {
        PojoComponentType type =
            new PojoComponentType(null);
        Constructor ctor = Foo.class.getConstructor(String.class);
        type.setConstructorDefinition(new ConstructorDefinition(ctor));
        propertyProcessor.visitConstructor(ctor, type, null);
        heuristicProcessor.visitEnd(Foo.class, type, null);
        assertEquals(1, type.getProperties().size());
        assertNotNull(type.getProperties().get("foo"));
    }

    protected void setUp() throws Exception {
        super.setUp();
        ImplementationProcessorServiceImpl service =
            new ImplementationProcessorServiceImpl(new JavaInterfaceProcessorRegistryImpl());
        propertyProcessor = new PropertyProcessor(service);
        heuristicProcessor = new HeuristicPojoProcessor(service, null);
    }

    public static class Foo {
        public Foo(@Property(name = "foo") String prop) {
        }
    }

}
