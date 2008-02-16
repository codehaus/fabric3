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

import java.util.List;

import org.fabric3.pojo.scdl.ConstructorDefinition;
import org.fabric3.pojo.scdl.JavaMappedProperty;
import org.fabric3.pojo.scdl.PojoComponentType;

import junit.framework.TestCase;
import org.fabric3.introspection.impl.contract.DefaultContractProcessor;

/**
 * Verifies constructors that have extensible annotation types, i.e. that have parameters marked by annotations which
 * are themselves processed by some other implementation processor
 *
 * @version $Rev$ $Date$
 */
public class HeutisticExtensibleConstructorTestCase extends TestCase {

    private HeuristicPojoProcessor processor;

    protected void setUp() throws Exception {
        super.setUp();
        DefaultContractProcessor contractProcessor = new DefaultContractProcessor();
        ImplementationProcessorServiceImpl processorService = new ImplementationProcessorServiceImpl(contractProcessor);
        processor = new HeuristicPojoProcessor(processorService, contractProcessor);
    }

    /**
     * Verifies heuristic processing can be called before an extension annotation processors is called.
     * <p/>
     * For example, given:
     * <pre> Foo(@Bar String prop, @org.osoa.sca.annotations.Property(name = "foo") String prop2)</pre>
     * <p/>
     * Heuristic evaluation of @Property can occur prior to another implementation processor evaluating @Bar
     *
     * @throws Exception
     */
    public void testBarAnnotationProcessedLast() throws Exception {
        PojoComponentType type =
            new PojoComponentType(null);
        processor.visitEnd(Foo.class, type, null);

        // now simulate process the bar impl
        ConstructorDefinition definition = type.getConstructorDefinition();
        List<String> injectionNames = definition.getInjectionNames();
        injectionNames.remove(0);
        injectionNames.add(0, "mybar");
        type.getProperties().put("mybar", new JavaMappedProperty<String>());

        assertEquals(2, type.getProperties().size());
        assertEquals("foo", definition.getInjectionNames().get(1));
    }

    public @interface Bar {

    }

    public static class Foo {
        public Foo(@Bar String prop, @org.osoa.sca.annotations.Property(name = "foo") String prop2) {
        }
    }

    public static class Foo2 {
        public Foo2(@org.osoa.sca.annotations.Reference(name = "baz") String prop1,
                    @Bar String prop2,
                    @org.osoa.sca.annotations.Property(name = "foo") String prop3) {
        }
    }


}


