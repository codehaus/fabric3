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

import org.fabric3.spi.component.ScopeRegistry;
import org.fabric3.pojo.processor.PojoComponentType;
import org.fabric3.pojo.processor.ProcessingException;
import org.fabric3.spi.model.type.Scope;

import junit.framework.TestCase;
import org.easymock.EasyMock;

/**
 * @version $Rev$ $Date$
 */
public class ScopeProcessorTestCase extends TestCase {

    private ScopeProcessor processor;
    private PojoComponentType type;
    private ScopeRegistry scopeRegistry;

    public void testScopeFromAnnotation() throws ProcessingException {
        scopeRegistry.getScope("COMPOSITE");
        EasyMock.expectLastCall().andReturn(Scope.COMPOSITE);
        EasyMock.replay(scopeRegistry);
        processor.visitClass(Composite.class, type, null);
        assertEquals(Scope.COMPOSITE, type.getImplementationScope());
        EasyMock.verify(scopeRegistry);
    }

    public void testScopeFromBareClass() throws ProcessingException {
        EasyMock.replay(scopeRegistry);
        processor.visitClass(None.class, type, null);
        assertNull(type.getImplementationScope());
        EasyMock.verify(scopeRegistry);
    }

    protected void setUp() throws Exception {
        super.setUp();
        scopeRegistry = EasyMock.createMock(ScopeRegistry.class);
        type = new PojoComponentType();
        processor = new ScopeProcessor(scopeRegistry);
    }

    @org.osoa.sca.annotations.Scope("COMPOSITE")
    private class Composite {
    }

    private class None {
    }

}
