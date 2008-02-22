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
import java.util.Map;
import java.io.Serializable;

import junit.framework.TestCase;
import org.osoa.sca.annotations.Remotable;
import org.osoa.sca.annotations.Service;
import org.easymock.EasyMock;

import org.fabric3.introspection.ContractProcessor;
import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.TypeMapping;
import org.fabric3.introspection.impl.contract.DefaultContractProcessor;
import org.fabric3.introspection.impl.DefaultIntrospectionHelper;
import org.fabric3.pojo.scdl.PojoComponentType;

/**
 * @version $Rev$ $Date$
 */
public class HeuristicConstructorTestCase extends TestCase {

    private HeuristicPojoProcessor processor;
    private ContractProcessor contractProcessor;
    private IntrospectionContext context;

    public void testBareConstructor() throws Exception {
        PojoComponentType type = new PojoComponentType(null);
        processor.visitEnd(Bare.class, type, context);
        assertTrue(type.getProperties().containsKey("Bare[0]"));
        assertTrue(type.getProperties().containsKey("Bare[1]"));
        assertTrue(type.getReferences().containsKey("Bare[2]"));
        assertTrue(type.getReferences().containsKey("Bare[3]"));
    }

    public static class Bare {
        public Bare(String val, Serializable prop, RemoteRef remote, LocalRef local) {
        }
    }

    public void testCollectionConstructor() throws Exception {
        PojoComponentType type = new PojoComponentType(null);
        processor.visitEnd(Multi.class, type, context);
        assertTrue(type.getProperties().containsKey("Multi[0]"));
        assertTrue(type.getReferences().containsKey("Multi[1]"));
        assertTrue(type.getReferences().containsKey("Multi[2]"));
    }

    public static class Multi {
        public Multi(String[] val, List<RemoteRef> remote, Map<String,LocalRef> local) {
        }
    }

    @Remotable
    public static interface RemoteRef {

    }

    @Service
    public static interface LocalRef {

    }

    protected void setUp() throws Exception {
        super.setUp();
        contractProcessor = new DefaultContractProcessor();
        DefaultIntrospectionHelper helper = new DefaultIntrospectionHelper();
        context = EasyMock.createMock(IntrospectionContext.class);
        TypeMapping typeMapping = new TypeMapping();
        EasyMock.expect(context.getTypeMapping()).andStubReturn(typeMapping);
        EasyMock.replay(context);
        ImplementationProcessorServiceImpl processorService = new ImplementationProcessorServiceImpl(contractProcessor, helper);
        processor = new HeuristicPojoProcessor(processorService);
    }
}
